CC = gcc
SRC = src
BUILD = build
BIN = $(BUILD)/bin
OBJ = $(BUILD)/obj
MAIN = $(BIN)/clox
SRCS = $(shell find $(SRC)/ -type f -name *.c)
OBJS = $(patsubst $(SRC)/%.c, $(OBJ)/%.o, $(SRCS))

.PHONY: all
all: $(MAIN)

$(MAIN): $(OBJS)
	@mkdir -p $(@D)
	$(CC) -o $@ $^

$(OBJ)/main.o: $(SRC)/main.c
	@mkdir -p $(@D)
	$(CC) -c $< -o $@

$(OBJ)/%.o: $(SRC)/%.c $(SRC)/%.h
	@mkdir -p $(@D)
	$(CC) -c $< -o $@

.PHONY: clean
clean:
	rm -rf $(BUILD)
