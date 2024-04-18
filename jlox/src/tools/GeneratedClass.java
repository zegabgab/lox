package tools;

import java.util.*;
import java.util.stream.*;

class GeneratedClass {
    final String name;
    final String baseClass;
    final List<GeneratedField> fields;

    GeneratedClass(String name, String baseClass, List<GeneratedField> fields) {
        this.name = name;
        this.baseClass = baseClass;
        this.fields = fields;
    }

    public static GeneratedClass make(String source, String baseClass) {
        var split = source.split(":");
        var name = split[0].strip();
        var fields = Arrays.stream(split[1].split(","))
                .map(String::strip)
                .map(s -> s.split(" "))
                .map(strings -> new GeneratedField(strings[0], strings[1]))
                .collect(Collectors.toList());

        return new GeneratedClass(name, baseClass, fields);
    }

    private String extending() {
        return Optional.ofNullable(baseClass)
                .map(name -> " extends " + name)
                .orElse("");
    }

    // TODO
    public String writeFormatted(CharSequence indent) {
        StringBuilder builder = new StringBuilder();

        builder.append("class ")
                .append(name)
                .append(extending())
                .append(" {\n");

        for (var field : fields) {
            builder.append(indent)
                    .append(field)
                    .append(';');
        }

        return builder.toString();
    }

    // TODO
    public List<String> writeFormattedLines(CharSequence indent) {
        return null;
    }
}
