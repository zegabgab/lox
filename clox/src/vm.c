#include <stdarg.h>
#include <stdio.h>
#include <string.h>

#include "common.h"
#include "compiler.h"
#include "debug.h"
#include "memory.h"
#include "object.h"
#include "value.h"
#include "vm.h"

VM vm;

static void resetStack(void) {
    vm.stackTop = vm.stack;
}

void initVM(void) {
    resetStack();
    vm.objects = NULL;
}

void freeVM(void) {
    freeObjects();
}

static void runtimeError(const char *format, ...) {
    va_list args;
    va_start(args, format);
    vfprintf(stderr, format, args);
    va_end(args);
    fputs("\n", stderr);

    size_t instruction = vm.ip - vm.chunk->code - 1;
    int line = getLine(vm.chunk, instruction);
    fprintf(stderr, "[line %d] in script\n", line);
    resetStack();
}

static Value peek(int distance) {
    return vm.stackTop[-1 - distance];
}

static void concatenate() {
    ObjString *two = AS_STRING(pop());
    ObjString *one = AS_STRING(pop());

    int length = one->length + two->length;
    char *chars = ALLOCATE(char, length + 1);
    memcpy(chars, one->chars, sizeof(char) * one->length);
    memcpy(chars + one->length, two->chars, sizeof(char) * (two->length + 1));

    ObjString *result = takeString(chars, length);
    push(OBJ_VAL(result));
}

static InterpretResult run(void) {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
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
        disassembleInstruction(vm.chunk, (int) (vm.ip - vm.chunk->code));
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
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                push(constant);
                break;
            }
            case OP_DIVIDE:
                BINARY_OP(NUMBER_VAL, /);
                break;
            case OP_FALSE:
                push(BOOL_VAL(false));
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
                Value value = pop();
                bool falsy = IS_NIL(value) || (IS_BOOL(value) && !AS_BOOL(value));
                push(BOOL_VAL(falsy));
                break;
            }
            case OP_RETURN:
                printValue(pop());
                printf("\n");
                return INTERPRET_OK;
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
#undef READ_CONSTANT
#undef READ_BYTE
}

InterpretResult interpret(const char *source) {
    Chunk chunk;
    initChunk(&chunk);

    if (!compile(source, &chunk)) {
        freeChunk(&chunk);
        return INTERPRET_COMPILE_ERROR;
    }

    vm.chunk = &chunk;
    vm.ip = chunk.code;

    InterpretResult result = run();

    freeChunk(&chunk);
    return result;
}

void push(Value value) {
    *vm.stackTop = value;
    vm.stackTop++;
}

Value pop(void) {
    vm.stackTop--;
    return *vm.stackTop;
}