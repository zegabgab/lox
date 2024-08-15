#include "chunk.h"
#include "memory.h"

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
    writeValueArray(&chunk->constants, value);
    return index;
}

int getLine(Chunk *chunk, int index) {
    for (int i = 0; i < chunk->lines.count; i++) {
        index -= chunk->lines.lines[i];
        if (index < 0) {
            return i + 1;
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

void writeLineArray(LineArray *array, int line) {
    line--;
    if (line >= array->capacity) {
        int oldCapacity = array->capacity;
        do {
            array->capacity = GROW_CAPACITY(array->capacity);
        } while (line >= array->capacity);

        array->lines = GROW_ARRAY(array->lines, oldCapacity, array->capacity);
    }

    for (int i = array->count; i <= line; i++) {
        array->lines[i] = 0;
    }

    array->count = line >= array->count ? line + 1 : array->count;
    array->lines[line]++;
}

