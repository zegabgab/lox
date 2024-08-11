package jlox;

import java.util.*;

public interface LoxCallable {
    Object call(Interpreter interpreter, List<Object> arguments);
    int arity();
}
