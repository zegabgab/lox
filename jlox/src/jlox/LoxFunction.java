package jlox;

import java.util.*;

class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    public LoxFunction bind(LoxInstance instance) {
        var environment = new Environment(closure);
        environment.define(instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (Object argument : arguments) {
            environment.define(argument);
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return result) {
            if (isInitializer) {
                return closure.getAt(0, 0);
            }
            return result.value();
        }

        if (isInitializer) {
            return closure.getAt(0, 0);
        }

        return null;
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
