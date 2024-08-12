package jlox;

class Ansi {
    public static String magenta() {
        return "\u001B[95m";
    }

    public static String reset() {
        return "\u001B[0m";
    }
}
