#ifndef CLOX_CHUNK_H
#define CLOX_CHUNK_H

#include "common.h"
#include "value.h"

typedef enum {
    OP_ADD,
    OP_CONSTANT,
    OP_DIVIDE,
    OP_FALSE,
    OP_MULTIPLY,
    OP_NEGATE,
    OP_NIL,
    OP_NOT,
    OP_POP,
    OP_PRINT,
    OP_RETURN,
    OP_SUBTRACT,
    OP_TRUE,
    OP_EQUAL,
    OP_GREATER,
    OP_LESS,
    OP_DEFINE_GLOBAL,
    OP_GET_GLOBAL,
    OP_SET_GLOBAL,
} OpCode;

typedef struct {
    int line;
    int run;
} LineRunLength;

typedef struct {
    LineRunLength *lines;
    int count;
    int capacity;
} LineArray;

typedef struct {
    ValueArray constants;
    LineArray lines;
    uint8_t *code;
    int count;
    int capacity;
} Chunk;

void initChunk(Chunk *chunk);
void freeChunk(Chunk *chunk);
void writeChunk(Chunk *chunk, uint8_t byte, int line);
int addConstant(Chunk *chunk, Value value);
int getLine(Chunk *chunk, int index);
void initLineArray(LineArray *array);
void freeLineArray(LineArray *array);
void writeLineArray(LineArray *array, int line);

#endif
