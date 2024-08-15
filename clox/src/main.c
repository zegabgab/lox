#include "common.h"
#include "chunk.h"
#include "debug.h"

int main(int argc, char **argv) {
    Chunk chunk;
    initChunk(&chunk);

    int constant = addConstant(&chunk, 6.9);
    writeChunk(&chunk, OP_CONSTANT, 420);
    writeChunk(&chunk, constant, 420);

    writeChunk(&chunk, OP_RETURN, 420);

    disassembleChunk(&chunk, "test chunk");
    freeChunk(&chunk);
    return 0;
}
