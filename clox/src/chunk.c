#include "chunk.h"
#include "memory.h"
#include "vm.h"

void initChunk(Chunk *chunk) {
    initValueArray(&chunk->constants);
    initLineArray(&chunk->lines);
    chunk->code = NULL;
    chunk->count = 0;
    chunk->capacity = 0;
}

void freeChunk(Chunk *chunk) {
    freeValueArray(&chunk->constants);
    freeLineArray(&chunk->lines);
    FREE_ARRAY(chunk->code, chunk->capacity);
    initChunk(chunk);
}

void writeChunk(Chunk *chunk, uint8_t byte, int line) {
    if (chunk->count >= chunk->capacity) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(chunk->code, oldCapacity, chunk->capacity);
    }

    chunk->code[chunk->count++] = byte;
    writeLineArray(&chunk->lines, line);
}

int addConstant(Chunk *chunk, Value value) {
    int index = chunk->constants.count;
    push(value);
    writeValueArray(&chunk->constants, value);
    pop();
    return index;
}

int getLine(Chunk *chunk, int index) {
    for (int i = 0; i < chunk->lines.count; i++) {
        index -= chunk->lines.lines[i].run;
        if (index < 0) {
            return chunk->lines.lines[i].line;
        }
    }

    return -1;
}

void initLineArray(LineArray *array) {
    array->lines = NULL;
    array->count = 0;
    array->capacity = 0;
}

void freeLineArray(LineArray *array) {
    FREE_ARRAY(array->lines, array->capacity);
    initLineArray(array);
}

static void addLine(LineArray *array, int line) {
    if (array->count >= array->capacity) {
        int oldCapacity = array->capacity;
        array->capacity = GROW_CAPACITY(oldCapacity);
        array->lines = 
            GROW_ARRAY(array->lines, oldCapacity, array->capacity);
    }
    array->lines[array->count] = 
        (LineRunLength) { .line = line, .run = 1 };
    array->count++;
}

void writeLineArray(LineArray *array, int line) {
    if (array->count == 0
            || array->lines[array->count - 1].line != line) {
        addLine(array, line);
    } else {
        array->lines[array->count - 1].run++;
    }
}
