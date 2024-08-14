package jlox;

import java.util.*;

class Environment {
    private final ArrayList<Object> values = new ArrayList<>();
    private final Environment outer;

    Environment(Environment outer) {
        this.outer = outer;
    }

    public void define(Object value) {
        values.add(value);
    }

    public Environment enclosing() {
        return outer;
    }

    private Environment ancestor(int distance) {
        Environment ancestor = this;
        for (int i = 0; i < distance; i++) {
            ancestor = ancestor.outer;
        }

        return ancestor;
    }

    public Object getAt(int distance, int index) {
        return ancestor(distance).values.get(index);
    }

    public Object assignAt(int distance, int index, Object value) {
        ancestor(distance).values.set(index, value);
        return value;
    }

    public void assignLast(Object value) {
        values.set(values.size() - 1, value);
    }
}
