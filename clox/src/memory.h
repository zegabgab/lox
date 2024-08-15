#ifndef CLOX_MEMORY_H
#define CLOX_MEMORY_H

#include "common.h"

#define GROW_CAPACITY(capacity) \
    ((capacity) < 8 ? 8 : ((capacity) * 2))

#define GROW_ARRAY(array, oldCount, newCount) \
    (typeof(array)) reallocate(array, (sizeof *(array)) * (oldCount), \
            (sizeof *(array)) * (newCount))

#define FREE_ARRAY(array, count) \
    reallocate(array, (sizeof *(array)) * (count), 0)

void *reallocate(void *array, size_t oldSize, size_t newSize);

#endif
