#ifndef CLOX_VM_H
#define CLOX_VM_H

#include "chunk.h"
#include "table.h"
#include "value.h"

#define STACK_MAX (1 << 8)

typedef struct {
    Chunk *chunk;
    uint8_t *ip;
    Value stack[STACK_MAX];
    Value *stackTop;
    Table strings;
    Obj *objects;
} VM;

extern VM vm;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR,
} InterpretResult;

void initVM(void);
void freeVM(void);
InterpretResult interpret(const char *source);
void push(Value value);
Value pop(void);

#endif
