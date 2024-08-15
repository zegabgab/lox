#include <stdio.h>

#include "memory.h"
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
    printf("%g", value);
}
