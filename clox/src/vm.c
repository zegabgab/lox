#include <stdio.h>

#include "common.h"
#include "compiler.h"
#include "debug.h"
#include "vm.h"

VM vm;

static void resetStack(void) {
    vm.stackTop = vm.stack;
}

void initVM(void) {
    resetStack();
}

void freeVM(void) {
}

static InterpretResult run(void) {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
#define BINARY_OP(op) \
    do { \
        double right = pop(); \
        double left = pop(); \
        push(left op right); \
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
            case OP_ADD:
                {
                    BINARY_OP(+);
                    break;
                }
            case OP_CONSTANT:
                {
                    Value constant = READ_CONSTANT();
                    push(constant);
                    break;
                }
            case OP_DIVIDE:
                {
                    BINARY_OP(/);
                    break;
                }
            case OP_MULTIPLY:
                {
                    BINARY_OP(*);
                    break;
                }
            case OP_NEGATE:
                {
                    push(-pop());
                    break;
                }
            case OP_RETURN:
                {
                    printValue(pop());
                    printf("\n");
                    return INTERPRET_OK;
                }
            case OP_SUBTRACT:
                {
                    BINARY_OP(-);
                    break;
                }
        }
    }

#undef BINARY_OP
#undef READ_CONSTANT
#undef READ_BYTE
}

InterpretResult interpret(const char *source) {
    compile(source);
    return INTERPRET_OK;
}

void push(Value value) {
    *vm.stackTop = value;
    vm.stackTop++;
}

Value pop(void) {
    vm.stackTop--;
    return *vm.stackTop;
}
