#include <stdio.h>
#include <string.h>

#include "common.h"
#include "scanner.h"

#define ERROR_TOKEN(message) (errorToken((message), sizeof (message)))

typedef struct {
    const char *start;
    const char *current;
    int line;
} Scanner;

Scanner scanner;

void initScanner(const char *source) {
    scanner.start = source;
    scanner.current = source;
    scanner.line = 1;
}

static bool isAtEnd() {
    return *scanner.current == '\0';
}

static char peek() {
    return *scanner.current;
}

static char peekNext() {
    return isAtEnd() ? '\0' : scanner.current[1];
}

static char advance() {
    return *(scanner.current++);
}

static bool match(char expected) {
    if (isAtEnd() || *scanner.current != expected) {
        return false;
    }

    scanner.current++;
    return true;
}

static Token makeToken(TokenType type) {
    Token token = {
        .type = type,
        .start = scanner.start,
        .length = (int) (scanner.current - scanner.start),
        .line = scanner.line,
    };
    return token;
}

static Token errorToken(const char *message, size_t length) {
    Token token = {
        .type = TOKEN_ERROR,
        .start = message,
        .length = length - 1,
        .line = scanner.line,
    };
    return token;
}

static void skipWhitespace(void) {
    for (;;) {
        char c = peek();
        switch (c) {
            case '\n':
                scanner.line++;
            case ' ':
            case '\r':
            case '\t':
                advance();
                break;
            case '/':
                if (peekNext() == '/') {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    return;
                }
                break;
            default:
                return;
        }
    }
}

static Token string() {
    while (peek() != '"' && !isAtEnd()) {
        if (peek() == '\n') {
            scanner.line++;
        }
        advance();
    }

    if (isAtEnd()) {
        return ERROR_TOKEN("Unterminated string");
    }

    advance();
    return makeToken(TOKEN_STRING);
}

static bool isDigit(char c) {
    return '0' <= c && c <= '9';
}

static bool isAlpha(char c) {
    return 'a' <= c && c <= 'z'
        || 'A' <= c && c <= 'Z'
        || c == '_';
}

static Token number() {
    while (isDigit(peek())) {
        advance();
    }

    if (peek() == '.' && isDigit(peekNext())) {
        advance();

        while (isDigit(peek())) {
            advance();
        }
    }

    return makeToken(TOKEN_NUMBER);
}

static TokenType findKeyword(const char *start, const char *tail, TokenType type) {
    for (; *tail != '\0'; start++, tail++) {
        if (start == scanner.current || *start != *tail) {
            return TOKEN_IDENTIFIER;
        }
    }

    return start == scanner.current ? type : TOKEN_IDENTIFIER;
}

static TokenType identifierType() {
    const char *start = scanner.start;
    switch (*start++) {
        case 'a': return findKeyword(start, "nd", TOKEN_AND);
        case 'c': return findKeyword(start, "lass", TOKEN_CLASS);
        case 'e': return findKeyword(start, "lse", TOKEN_ELSE);
        case 'f': switch (*start++) {
                      case 'a': return findKeyword(start, "lse", TOKEN_FALSE);
                      case 'o': return findKeyword(start, "r", TOKEN_FOR);
                      case 'u': return findKeyword(start, "n", TOKEN_FUN);
                  }
        case 'i': return findKeyword(start, "f", TOKEN_IF);
        case 'n': return findKeyword(start, "il", TOKEN_NIL);
        case 'o': return findKeyword(start, "r", TOKEN_OR);
        case 'p': return findKeyword(start, "rint", TOKEN_PRINT);
        case 'r': return findKeyword(start, "eturn", TOKEN_RETURN);
        case 's': return findKeyword(start, "uper", TOKEN_SUPER);
        case 't': switch (*start++) {
                      case 'h': return findKeyword(start, "is", TOKEN_THIS);
                      case 'r': return findKeyword(start, "ue", TOKEN_TRUE);
                  }
        case 'v': return findKeyword(start, "ar", TOKEN_VAR);
        case 'w': return findKeyword(start, "hile", TOKEN_WHILE);
    }

    return TOKEN_IDENTIFIER;
}

static Token identifier() {
    while (isAlpha(peek()) || isDigit(peek())) {
        advance();
    }

    return makeToken(identifierType());
}

Token scanToken(void) {
    skipWhitespace();
    scanner.start = scanner.current;

    if (isAtEnd()) {
        return makeToken(TOKEN_EOF);
    }

    char c = advance();

    if (isDigit(c)) {
        return number();
    }
    if (isAlpha(c)) {
        return identifier();
    }

    switch (c) {
        case '(': return makeToken(TOKEN_LEFT_PAREN);
        case ')': return makeToken(TOKEN_RIGHT_PAREN);
        case '{': return makeToken(TOKEN_LEFT_BRACE);
        case '}': return makeToken(TOKEN_RIGHT_BRACE);
        case ',': return makeToken(TOKEN_COMMA);
        case '.': return makeToken(TOKEN_DOT);
        case ';': return makeToken(TOKEN_SEMICOLON);
        case '-': return makeToken(TOKEN_MINUS);
        case '+': return makeToken(TOKEN_PLUS);
        case '/': return makeToken(TOKEN_SLASH);
        case '*': return makeToken(TOKEN_STAR);

        case '!': return makeToken(
                          match('=') ? TOKEN_BANG_EQUAL : TOKEN_BANG);
        case '=': return makeToken(
                          match('=') ? TOKEN_EQUAL_EQUAL : TOKEN_EQUAL);
        case '<': return makeToken(
                          match('=') ? TOKEN_LESS_EQUAL : TOKEN_LESS);
        case '>': return makeToken(
                          match('=') ? TOKEN_GREATER_EQUAL : TOKEN_GREATER);
        case '"': return string();
    }

    return ERROR_TOKEN("Unexpected character");
}
