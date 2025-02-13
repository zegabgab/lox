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
#ifdef NAN_BOXING
    if (IS_BOOL(value)) {
        printf(AS_BOOL(value) ? "true" : "false");
    } else if (IS_NIL(value)) {
        printf("nil");
    } else if (IS_NUMBER(value)) {
        printf("%g", AS_NUMBER(value));
    } else if (IS_OBJ(value)) {
        printObject(value);
    }
#else
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
#endif
}

bool valuesEqual(Value one, Value two) {
#ifdef NAN_BOXING
    if (IS_NUMBER(one) && IS_NUMBER(two)) {
        return AS_NUMBER(one) == AS_NUMBER(two);
    }
    return one == two;
#else
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
        case VAL_OBJ:
            return AS_OBJ(one) == AS_OBJ(two);
    }

    return false;
#endif
}
