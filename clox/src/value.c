#include <stdio.h>
#include <string.h>

#include "memory.h"
#include "object.h"
#include "value.h"

void initValueArray(ValueArray *array) {
    array->values = NULL;
    array->count = 0;
    array->capacity = 0;
}

void freeValueArray(ValueArray *array) {
    FREE_ARRAY(array->values, array->capacity);
    initValueArray(array);
}

void writeValueArray(ValueArray *array, Value value) {
    if (array->count >= array->capacity) {
        int oldCapacity = array->capacity;
        array->capacity = GROW_CAPACITY(oldCapacity);
        array->values = GROW_ARRAY(array->values, oldCapacity, array->capacity);
    }
    
    array->values[array->count++] = value;
}

void printValue(Value value) {
    switch (value.type) {
        case VAL_NUMBER:
            printf("%g", AS_NUMBER(value));
            break;
        case VAL_BOOL:
            printf(AS_BOOL(value) ? "true" : "false");
            break;
        case VAL_NIL:
            printf("nil");
            break;
        case VAL_OBJ:
            printObject(value);
            break;
    }
}

bool valuesEqual(Value one, Value two) {
    if (one.type != two.type) {
        return false;
    }
    switch (one.type) {
        case VAL_NIL:
            return true;
        case VAL_BOOL:
            return AS_BOOL(one) == AS_BOOL(two);
        case VAL_NUMBER:
            return AS_NUMBER(one) == AS_NUMBER(two);
        case VAL_OBJ: {
            ObjString *left = AS_STRING(one);
            ObjString *right = AS_STRING(two);
            return left->length == right->length &&
                memcmp(left->chars, right->chars, left->length) == 0;
        }
    }
}
