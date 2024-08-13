package jlox;

import java.util.*;

public class GlobalEnvironment {
    private final HashMap<String, Object> values = new HashMap<>();

    private void assertDefined(Token name) {
        if (!values.containsKey(name.lexeme)) {
            throw new RuntimeError(name, "Undefined variable");
        }
    }

    public Object get(Token name) {
        assertDefined(name);
        return values.get(name.lexeme);
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object assign(Token name, Object value) {
        assertDefined(name);
        values.put(name.lexeme, value);
        return value;
    }
}
