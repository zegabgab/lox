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

static void freeClosure(ObjClosure *closure) {
    FREE_ARRAY(closure->upvalues, closure->upvalueCount);
    FREE(closure);
}

static void freeFunction(ObjFunction *function) {
    freeChunk(&function->chunk);
    FREE(function);
}

static void freeNative(ObjNative *native) {
    FREE(native);
}

static void freeString(ObjString *string) {
    FREE_ARRAY(string->chars, string->length + 1);
    FREE(string);
}

static void freeUpvalue(ObjUpvalue *upvalue) {
    FREE(upvalue);
}

static void freeObject(Obj *object) {
    switch (object->type) {
        case OBJ_CLOSURE:
            freeClosure((ObjClosure*) object);
            break;
        case OBJ_FUNCTION:
            freeFunction((ObjFunction*) object);
            break;
        case OBJ_NATIVE:
            freeNative((ObjNative*) object);
            break;
        case OBJ_STRING:
            freeString((ObjString*) object);
            break;
        case OBJ_UPVALUE:
            freeUpvalue((ObjUpvalue*) object);
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
