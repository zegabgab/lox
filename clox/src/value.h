#ifndef CLOX_VALUE_H
#define CLOX_VALUE_H

#include "common.h"

typedef enum {
    VAL_BOOL,
    VAL_NIL,
    VAL_NUMBER,
    VAL_OBJ,
} ValueType;

typedef struct Obj Obj;
typedef struct ObjString ObjString;

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
