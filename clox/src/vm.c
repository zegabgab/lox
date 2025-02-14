#include <assert.h>
#include <stdarg.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

#include "common.h"
#include "compiler.h"
#include "debug.h"
#include "memory.h"
#include "object.h"
#include "value.h"
#include "vm.h"

VM vm;

static Value clockNative(int argCount, Value *args) {
    return NUMBER_VAL((double) clock() / CLOCKS_PER_SEC);
}

static void resetStack(void) {
    vm.stackTop = vm.stack;
    vm.frameCount = 0;
}

static void defineNative(const char *name, NativeFn function);

void initVM(void) {
    resetStack();
    vm.openUpvalues = NULL;
    vm.objects = NULL;
    vm.bytesAllocated = 0;
    vm.nextGC = 2 << 20;

    vm.grayCount = 0;
    vm.grayCapacity = 0;
    vm.grayStack = NULL;

    initTable(&vm.globals);
    initTable(&vm.strings);

    vm.initString = NULL;
    vm.initString = copyString("init", 4);

    defineNative("clock", clockNative);
}

void freeVM(void) {
    freeTable(&vm.globals);
    freeTable(&vm.strings);
    vm.initString = NULL;
    freeObjects();
}

static void runtimeError(const char *format, ...) {
    va_list args;
    va_start(args, format);
    vfprintf(stderr, format, args);
    va_end(args);
    fputs("\n", stderr);

    for (int i = vm.frameCount - 1; i >= 0; i--) {
        CallFrame *frame = vm.frames + i;
        ObjFunction *function = frame->closure->function;
        size_t instruction = frame->ip - function->chunk.code - 1;
        int line = getLine(&function->chunk, instruction);
        fprintf(stderr, "[line %d] in ", line);
        if (function->name == NULL) {
            fprintf(stderr, "script\n");
        } else {
            fprintf(stderr, "%s()\n", function->name->chars);
        }
    }

    resetStack();
}

static void defineNative(const char *name, NativeFn function) {
    push(OBJ_VAL(copyString(name, (int) strlen(name))));
    push(OBJ_VAL(newNative(function)));
    tableSet(&vm.globals, AS_STRING(vm.stack[0]), vm.stack[1]);
    pop();
    pop();
}

static Value peek(int distance) {
    return vm.stackTop[-1 - distance];
}

static void concatenate() {
    ObjString *two = AS_STRING(peek(0));
    ObjString *one = AS_STRING(peek(1));

    int length = one->length + two->length;
    char *chars = ALLOCATE(char, length + 1);
    memcpy(chars, one->chars, sizeof(char) * one->length);
    memcpy(chars + one->length, two->chars, sizeof(char) * (two->length + 1));

    ObjString *result = takeString(chars, length);
    pop();
    pop();
    push(OBJ_VAL(result));
}

static bool isFalsey(Value value) {
    return IS_NIL(value) || (IS_BOOL(value) && !AS_BOOL(value));
}

static bool call(ObjClosure *closure, int argCount) {
    ObjFunction *function = closure->function;
    if (argCount != function->arity) {
        runtimeError("Expected %d arguments but got %d",
                function->arity,
                argCount);
        return false;
    }

    if (vm.frameCount == FRAMES_MAX) {
        runtimeError("Stack overflow");
        return false;
    }

    CallFrame *frame = vm.frames + vm.frameCount++;
    frame->closure = closure;
    frame->ip = function->chunk.code;
    frame->slots = vm.stackTop - argCount - 1;
    return true;
}

static bool callClass(ObjClass *klass, int argCount) {
    vm.stackTop[-argCount - 1] = OBJ_VAL(newInstance(klass));
    Value initializer;
    if (tableGet(&klass->methods, vm.initString, &initializer)) {
        return call(AS_CLOSURE(initializer), argCount);
    } else if (argCount > 0) {
        runtimeError("Expected 0 arguments but got %d", argCount);
        return false;
    }

    return true;
}

static bool callNative(NativeFn function, int argCount) {
    Value result = function(argCount, vm.stackTop - argCount);
    vm.stackTop -= argCount + 1;
    push(result);
    return true;
}

static bool callMethod(ObjBoundMethod *bound, int argCount) {
    vm.stackTop[-argCount - 1] = bound->receiver;
    return call(bound->method, argCount);
}

static bool notCallable(void) {
    runtimeError("Can only call functions and classes");
    return false;
}

static bool callValue(Value callee, int argCount) {
    if (!IS_OBJ(callee)) {
        return notCallable();
    }
    
    switch (OBJ_TYPE(callee)) {
        case OBJ_BOUND_METHOD:
            return callMethod(AS_BOUND_METHOD(callee), argCount);
        case OBJ_CLASS:
            return callClass(AS_CLASS(callee), argCount);
        case OBJ_CLOSURE:
            return call(AS_CLOSURE(callee), argCount);
        case OBJ_NATIVE:
            return callNative(AS_NATIVE(callee), argCount);
        default:
            return notCallable();
    }
}

static bool invokeFromClass(ObjClass *klass, ObjString *name, int argCount) {
    Value method;
    if (!tableGet(&klass->methods, name, &method)) {
        runtimeError("Undefined property '%s'", name->chars);
        return false;
    }

    return call(AS_CLOSURE(method), argCount);
}

static bool invoke(ObjString *name, int argCount) {
    Value receiver = peek(argCount);

    if (!IS_INSTANCE(receiver)) {
        runtimeError("Only instances have methods");
        return false;
    }

    ObjInstance *instance = AS_INSTANCE(receiver);

    Value value;
    if (tableGet(&instance->fields, name, &value)) {
        vm.stackTop[-argCount - 1] = value;
        return callValue(value, argCount);
    }

    return invokeFromClass(instance->klass, name, argCount);
}

static bool bindMethod(ObjClass *klass, ObjString *name) {
    Value method;
    if (!tableGet(&klass->methods, name, &method)) {
        runtimeError("Undefined property '%s'", name->chars);
        return false;
    }

    ObjBoundMethod *bound = newBoundMethod(peek(0), AS_CLOSURE(method));
    pop();
    push(OBJ_VAL(bound));
    return true;
}

static bool runCall(int argCount) {
    return callValue(peek(argCount), argCount);
}

static void closeUpvalues(Value *last) {
    while (vm.openUpvalues != NULL && vm.openUpvalues->location >= last) {
        ObjUpvalue *upvalue = vm.openUpvalues;
        upvalue->closed = *upvalue->location;
        upvalue->location = &upvalue->closed;
        vm.openUpvalues = upvalue->next;
    }
}

static void defineMethod(ObjString *name) {
    Value method = peek(0);
    ObjClass *klass = AS_CLASS(peek(1));
    tableSet(&klass->methods, name, method);
    pop();
}

static bool runReturn(CallFrame *frame) {
    Value result = pop();
    closeUpvalues(frame->slots);
    vm.frameCount--;
    if (vm.frameCount == 0) {
        pop();
        return true;
    }

    vm.stackTop = frame->slots;
    push(result);
    return false;
}

static ObjUpvalue *captureUpvalue(Value *local) {
    ObjUpvalue *prevUpvalue = NULL;
    ObjUpvalue *upvalue = vm.openUpvalues;
    while (upvalue != NULL && upvalue->location > local) {
        prevUpvalue = upvalue;
        upvalue = upvalue->next;
    }

    if (upvalue != NULL && upvalue->location == local) {
        return upvalue;
    }

    ObjUpvalue *createdUpvalue = newUpvalue(local);

    if (prevUpvalue == NULL) {
        vm.openUpvalues = createdUpvalue;
    } else {
        prevUpvalue->next = createdUpvalue;
    }

    return createdUpvalue;
}

static InterpretResult run(void) {
    CallFrame *frame = vm.frames + vm.frameCount - 1;

#define READ_BYTE() (*frame->ip++)
#define READ_CONSTANT() (frame->closure->function->chunk.constants.values[READ_BYTE()])
#define READ_STRING() AS_STRING(READ_CONSTANT())
#define READ_SHORT() \
    (frame->ip += 2, (int16_t) ((frame->ip[-2] << 8) | frame->ip[-1]))
#define BINARY_OP(valueType, op) \
    do { \
        if (!(IS_NUMBER(peek(0)) && IS_NUMBER(peek(1)))) { \
            runtimeError("Operands must be numbers"); \
            return INTERPRET_RUNTIME_ERROR; \
        } \
        Value right = pop(); \
        Value left = pop(); \
        push(valueType(AS_NUMBER(left) op AS_NUMBER(right))); \
    } while (false)

    for (;;) {
        uint8_t instruction;
#ifdef DEBUG_TRACE_EXECUTION
        printf("          ");
        for (Value *slot = vm.stack; slot < vm.stackTop; slot++) {
            printf("[ ");
            printValue(*slot);
            printf(" ]");
        }
        printf("\n");
        disassembleInstruction(&frame->closure->function->chunk,
                (int) (frame->ip - frame->closure->function->chunk.code));
#endif
        switch (instruction = READ_BYTE()) {
            case OP_ADD: {
                if (IS_STRING(peek(0)) && IS_STRING(peek(1))) {
                    concatenate();
                } else if (IS_NUMBER(peek(0)) && IS_NUMBER(peek(1))) {
                    double right = AS_NUMBER(pop());
                    double left = AS_NUMBER(pop());
                    push(NUMBER_VAL(left + right));
                } else {
                    runtimeError("Operands must be two numbers or two strings");
                    return INTERPRET_RUNTIME_ERROR;
                }
                break;
            }
            case OP_CALL:
                if (!runCall(READ_BYTE())) return INTERPRET_RUNTIME_ERROR;
                frame = vm.frames + vm.frameCount - 1;
                break;
            case OP_CLASS:
                push(OBJ_VAL(newClass(READ_STRING())));
                break;
            case OP_CLOSE_UPVALUE:
                closeUpvalues(vm.stackTop - 1);
                pop();
                break;
            case OP_CLOSURE: {
                ObjFunction *function = AS_FUNCTION(READ_CONSTANT());
                ObjClosure *closure = newClosure(function);
                push(OBJ_VAL(closure));
                for (int i = 0; i < closure->upvalueCount; i++) {
                    uint8_t isLocal = READ_BYTE();
                    uint8_t index = READ_BYTE();
                    if (isLocal) {
                        closure->upvalues[i] = captureUpvalue(frame->slots + index);
                    } else {
                        closure->upvalues[i] = frame->closure->upvalues[index];
                    }
                }
                break;
            }
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                push(constant);
                break;
            }
            case OP_DEFINE_GLOBAL: {
                ObjString *name = READ_STRING();
                tableSet(&vm.globals, name, peek(0));
                pop();
                break;
            }
            case OP_GET_GLOBAL: {
                ObjString *name = READ_STRING();
                assert(name != NULL && name->chars != NULL);
                Value value;
                if (!tableGet(&vm.globals, name, &value)) {
                    runtimeError("Undefined variable '%s'", name->chars);
                    return INTERPRET_RUNTIME_ERROR;
                }

                push(value);
                break;
            }
            case OP_GET_LOCAL: {
                uint8_t slot = READ_BYTE();
                push(frame->slots[slot]);
                break;
            }
            case OP_GET_PROPERTY: {
                if (!IS_INSTANCE(peek(0))) {
                    runtimeError("Only instances have properties");
                    return INTERPRET_RUNTIME_ERROR;
                }

                ObjInstance *instance = AS_INSTANCE(peek(0));
                ObjString *name = READ_STRING();

                Value value;
                if (tableGet(&instance->fields, name, &value)) {
                    pop();
                    push(value);
                    break;
                }

                if (!bindMethod(instance->klass, name)) {
                    return INTERPRET_RUNTIME_ERROR;
                }
                break;
            }
            case OP_GET_UPVALUE: {
                uint8_t slot = READ_BYTE();
                push(*frame->closure->upvalues[slot]->location);
                break;
            }
            case OP_SET_GLOBAL: {
                ObjString *name = READ_STRING();
                Value old;
                if (!tableGet(&vm.globals, name, &old)) {
                    runtimeError("Undefined variable '%s'\n", name->chars);
                    return INTERPRET_RUNTIME_ERROR;
                }
                tableSet(&vm.globals, name, peek(0));
                break;
            }
            case OP_SET_LOCAL: {
                uint8_t slot = READ_BYTE();
                frame->slots[slot] = peek(0);
                break;
            }
            case OP_SET_PROPERTY: {
                if (!IS_INSTANCE(peek(1))) {
                    runtimeError("Only instances have fields");
                    return INTERPRET_RUNTIME_ERROR;
                }
                ObjInstance *instance = AS_INSTANCE(peek(1));
                ObjString *name = READ_STRING();
                tableSet(&instance->fields, name, peek(0));
                Value value = pop();
                pop();
                push(value);
                break;
            }
            case OP_SET_UPVALUE: {
                uint8_t slot = READ_BYTE();
                *frame->closure->upvalues[slot]->location = peek(0);
                break;
            }
            case OP_DIVIDE:
                BINARY_OP(NUMBER_VAL, /);
                break;
            case OP_FALSE:
                push(BOOL_VAL(false));
                break;
            case OP_GET_SUPER: {
                ObjString *name = READ_STRING();
                ObjClass *superclass = AS_CLASS(pop());

                if (!bindMethod(superclass, name)) {
                    return INTERPRET_RUNTIME_ERROR;
                }
                break;
            }
            case OP_SUPER_INVOKE: {
                ObjString *method = READ_STRING();
                int argCount = READ_BYTE();
                ObjClass *superclass = AS_CLASS(pop());
                if (!invokeFromClass(superclass, method, argCount)) {
                    return INTERPRET_RUNTIME_ERROR;
                }
                frame = &vm.frames[vm.frameCount - 1];
                break;
            }
            case OP_INHERIT: {
                Value superclass = peek(1);
                if (!IS_CLASS(superclass)) {
                    runtimeError("Superclass must be a class");
                    return INTERPRET_RUNTIME_ERROR;
                }

                ObjClass *subclass = AS_CLASS(peek(0));
                tableAddAll(&AS_CLASS(superclass)->methods, &subclass->methods);
                pop();
                break;
            }
            case OP_INVOKE: {
                ObjString *method = READ_STRING();
                int argCount = READ_BYTE();
                if (!invoke(method, argCount)) {
                    return INTERPRET_RUNTIME_ERROR;
                }
                frame = &vm.frames[vm.frameCount - 1];
                break;
            }
            case OP_JUMP: {
                uint16_t offset = READ_SHORT();
                frame->ip += offset;
                break;
            }
            case OP_JUMP_IF_FALSE: {
                uint16_t offset = READ_SHORT();
                if (isFalsey(peek(0))) {
                    frame->ip += offset;
                }
                break;
            }
            case OP_LOOP: {
                uint16_t offset = READ_SHORT();
                frame->ip -= offset;
                break;
            }
            case OP_METHOD:
                defineMethod(READ_STRING());
                break;
            case OP_MULTIPLY:
                BINARY_OP(NUMBER_VAL, *);
                break;
            case OP_NEGATE:
                if (!IS_NUMBER(peek(0))) {
                    runtimeError("Operand must be a number");
                    return INTERPRET_RUNTIME_ERROR;
                }
                Value value = pop();
                push(NUMBER_VAL(-AS_NUMBER(value)));
                break;
            case OP_NIL:
                push(NIL_VAL);
                break;
            case OP_NOT: {
                bool falsy = isFalsey(pop());
                push(BOOL_VAL(falsy));
                break;
            }
            case OP_POP:
                pop();
                break;
            case OP_PRINT:
                printValue(pop());
                printf("\n");
                break;
            case OP_RETURN:
                if (runReturn(frame)) {
                    return INTERPRET_OK;
                }
                frame = vm.frames + vm.frameCount - 1;
                break;
            case OP_SUBTRACT:
                BINARY_OP(NUMBER_VAL, -);
                break;
            case OP_TRUE:
                push(BOOL_VAL(true));
                break;
            case OP_EQUAL: {
                Value two = pop();
                Value one = pop();
                push(BOOL_VAL(valuesEqual(one, two)));
                break;
            }
            case OP_LESS:
                BINARY_OP(BOOL_VAL, <);
                break;
            case OP_GREATER:
                BINARY_OP(BOOL_VAL, >);
                break;
        }
    }

#undef BINARY_OP
#undef READ_SHORT
#undef READ_STRING
#undef READ_CONSTANT
#undef READ_BYTE
}

InterpretResult interpret(const char *source) {
    ObjFunction *function = compile(source);

    if (function == NULL) {
        return INTERPRET_COMPILE_ERROR;
    }

    push(OBJ_VAL(function));
    ObjClosure *closure = newClosure(function);
    pop();
    push(OBJ_VAL(closure));
    call(closure, 0);

    return run();
}

void push(Value value) {
    *vm.stackTop = value;
    vm.stackTop++;
}

Value pop(void) {
    vm.stackTop--;
    return *vm.stackTop;
}
