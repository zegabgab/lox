#include <stdlib.h>

#include "memory.h"
#include "object.h"
#include "vm.h"

void *reallocate(void *array, size_t oldSize, size_t newSize) {
    if (newSize == 0) {
        free(array);
        return NULL;
    }

    void *result = realloc(array, newSize);
    if (result == NULL) {
        exit(1);
    }

    return result;
}

static void freeFunction(ObjFunction *function) {
    freeChunk(&function->chunk);
    FREE(function);
}

static void freeString(ObjString *string) {
    FREE_ARRAY(string->chars, string->length + 1);
    FREE(string);
}

static void freeObject(Obj *object) {
    switch (object->type) {
        case OBJ_FUNCTION:
            freeFunction((ObjFunction*) object);
            break;
        case OBJ_STRING:
            freeString((ObjString*) object);
            break;
    }
}

void freeObjects() {
    Obj *object = vm.objects;
    while (object != NULL) {
        Obj *next = object->next;
        freeObject(object);
        object = next;
    }
}
