package jlox;

import java.util.*;

class Environment {
    private final HashMap<String, Object> values = new HashMap<>();

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name, String.format("Undefined variable '%s'", name.lexeme));
    }
}
