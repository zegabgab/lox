package jlox;

import java.util.*;

class LoxInstance {
    private final LoxClass klass;
    private final HashMap<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    public Object get(Token property) {
        if (fields.containsKey(property.lexeme)) {
            return fields.get(property.lexeme);
        }

        var method = klass.findMethod(property.lexeme);
        if (method != null) {
            return method.bind(this);
        }

        throw new RuntimeError(property, "Undefined property '" + property.lexeme + "'");
    }

    public void set(String property, Object value) {
        fields.put(property, value);
    }

    @Override
    public String toString() {
        return klass + " instance";
    }
}
