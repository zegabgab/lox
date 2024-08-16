#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

int main(int argc, char **argv) {
    initVM();

    Chunk chunk;
    initChunk(&chunk);

    int constant = addConstant(&chunk, 1.2);
    writeChunk(&chunk, OP_CONSTANT, 420);
    writeChunk(&chunk, constant, 420);

    constant = addConstant(&chunk, 3.4);
    writeChunk(&chunk, OP_CONSTANT, 420);
    writeChunk(&chunk, constant, 420);

    writeChunk(&chunk, OP_ADD, 420);

    constant = addConstant(&chunk, 5.6);
    writeChunk(&chunk, OP_CONSTANT, 420);
    writeChunk(&chunk, constant, 420);

    writeChunk(&chunk, OP_DIVIDE, 420);

    writeChunk(&chunk, OP_NEGATE, 420);

    writeChunk(&chunk, OP_RETURN, 420);

    disassembleChunk(&chunk, "test chunk");
    interpret(&chunk);
    freeVM();
    freeChunk(&chunk);
    return 0;
}
