#ifndef CLOX_VALUE_H
#define CLOX_VALUE_H

#include "common.h"

#ifdef NAN_BOXING
#include <string.h>
#endif

typedef enum {
    VAL_BOOL,
    VAL_NIL,
    VAL_NUMBER,
    VAL_OBJ,
} ValueType;

typedef struct Obj Obj;
typedef struct ObjString ObjString;

#ifdef NAN_BOXING

#define QNAN     ((uint64_t) 0x7ffc000000000000)
#define SIGN_BIT ((uint64_t) 0x8000000000000000)

#define TAG_NIL 1
#define TAG_FALSE 2
#define TAG_TRUE 3

typedef uint64_t Value;

#define IS_NIL(value) ((value) == NIL_VAL)
#define IS_BOOL(value) (((value) | 1) == TRUE_VAL)
#define IS_FALSE(value) ((value) == FALSE_VAL)
#define IS_TRUE(value) ((value) == TRUE_VAL)
#define IS_NUMBER(value) (((value) & QNAN) != QNAN)
#define IS_OBJ(value) (((value) & (QNAN | SIGN_BIT)) == (QNAN | SIGN_BIT))

#define AS_NUMBER(value) valueToNum(value)
#define AS_BOOL(value) ((value) == TRUE_VAL)
#define AS_OBJ(value) ((Obj*) (uintptr_t) ((value) & ~(SIGN_BIT | QNAN)))

static inline double valueToNum(Value value) {
    double num;
    memcpy(&num, &value, sizeof value);
    return num;
}

#define NIL_VAL ((Value) (uint64_t) (QNAN | TAG_NIL))
#define FALSE_VAL ((Value) (uint64_t) (QNAN | TAG_FALSE))
#define TRUE_VAL ((Value) (uint64_t) (QNAN | TAG_TRUE))
#define BOOL_VAL(b) ((b) ? TRUE_VAL : FALSE_VAL)
#define NUMBER_VAL(num) numToValue(num)
#define OBJ_VAL(obj) (Value) (SIGN_BIT | QNAN | (uint64_t) (uintptr_t) (obj))

static inline Value numToValue(double num) {
    Value value;
    memcpy(&value, &num, sizeof num);
    return value;
}

#else

typedef struct {
    ValueType type;
    union {
        bool boolean;
        double number;
        Obj *obj;
    } as;
} Value;

#define IS_TYPE(value, valType) ((value).type == (valType))

#define IS_BOOL(value) IS_TYPE((value), VAL_BOOL)
#define IS_NIL(value) IS_TYPE((value), VAL_NIL)
#define IS_NUMBER(value) IS_TYPE((value), VAL_NUMBER)
#define IS_OBJ(value) IS_TYPE((value), VAL_OBJ)

#define AS_BOOL(value) ((value).as.boolean)
#define AS_NUMBER(value) ((value).as.number)
#define AS_OBJ(value) ((value).as.obj)

#define BOOL_VAL(value) ((Value) { .type = VAL_BOOL, .as = { .boolean = (value) } })
#define NIL_VAL ((Value) { .type = VAL_NIL })
#define NUMBER_VAL(value) ((Value) { .type = VAL_NUMBER, .as = { .number = (value) } })
#define OBJ_VAL(object) ((Value) { .type = VAL_OBJ, .as = { .obj = (Obj*) (object) } })

#endif

typedef struct {
    Value *values;
    int count;
    int capacity;
} ValueArray;

void initValueArray(ValueArray *array);
void freeValueArray(ValueArray *array);
void writeValueArray(ValueArray *array, Value value);
void printValue(Value value);
bool valuesEqual(Value one, Value two);

#endif
