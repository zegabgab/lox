#include <stdlib.h>

#include "compiler.h"
#include "memory.h"
#include "object.h"
#include "vm.h"

#ifdef DEBUG_LOG_GC
#include <stdio.h>
#include "debug.h"
#endif /* debug logging */

void *reallocate(void *array, size_t oldSize, size_t newSize) {
    if (newSize > oldSize) {
#ifdef DEBUG_STRESS_GC
        collectGarbage();
#endif
    }

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

void markObject(Obj *object) {
    if (object == NULL) {
        return;
    }

#ifdef DEBUG_LOG_GC
    printf("%p mark ", object);
    printValue(OBJ_VAL(object));
    printf("\n");
#endif

    object->isMarked = true;
}

void markValue(Value value) {
    if (IS_OBJ(value)) {
        markObject(AS_OBJ(value));
    }
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
#ifdef DEBUG_LOG_GC
    printf("%p free type %d\n", object, object->type);
#endif

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

static void markRoots() {
    for (Value *slot = vm.stack; slot < vm.stackTop; slot++) {
        markValue(*slot);
    }

    for (int i = 0; i < vm.frameCount; i++) {
        markObject((Obj*) vm.frames[i].closure);
    }

    for (ObjUpvalue *upvalue = vm.openUpvalues;
            upvalue != NULL;
            upvalue = upvalue->next) {
        markObject((Obj*) upvalue);
    }

    markTable(&vm.globals);
    markCompilerRoots();
}

void collectGarbage() {
#ifdef DEBUG_LOG_GC
    printf("-- gc begin\n");
#endif

    markRoots();

#ifdef DEBUG_LOG_GC
    printf("-- gc end\n");
#endif
}

void freeObjects() {
    Obj *object = vm.objects;
    while (object != NULL) {
        Obj *next = object->next;
        freeObject(object);
        object = next;
    }
}
