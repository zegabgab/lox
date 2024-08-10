package jlox;

import java.util.*;

class Environment {
    private final HashMap<String, Object> values = new HashMap<>();
    private final Environment outer;

    Environment(Environment outer) {
        this.outer = outer;
    }

    Environment() {
        outer = null;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (outer != null) {
            return outer.get(name);
        }

        throw new RuntimeError(name, String.format("Undefined variable '%s'", name.lexeme));
    }

    public Object assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else if (outer != null) {
            outer.assign(name, value);
        } else {
            throw new RuntimeError(name, String.format("Undefined variable '%s'", name.lexeme));
        }

        return value;
    }

    public Environment outer() {
        return outer;
    }
}
