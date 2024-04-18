package tools;

public class GeneratedField {
    final String type;
    final String name;

    public GeneratedField(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
