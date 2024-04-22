package tools;

import java.util.*;

public class GeneratedMethod {
    String type;
    String name;
    boolean isStatic;
    List<String> annotations;
    String accessModifier;
    List<String> body;
    List<GeneratedField> parameters;

    private String staticModifier() {
        return isStatic ? "static " : "";
    }

    private String accessModifier() {
        return accessModifier != null ? accessModifier + " " : "";
    }

    private String parameterList() {
        return parameters.stream()
                .map(GeneratedField::toString)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    public List<String> writeFormattedLines(CharSequence indent) {
        var lines = new ArrayList<>(annotations);
        lines.add(accessModifier() + staticModifier() + type + " " + name + "(" + parameterList() + ") {");
        for (var line : body) {
            lines.add(indent + line);
        }
        lines.add("}");
        return lines;
    }
}
