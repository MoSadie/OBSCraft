package com.mosadie.obscraft.actions.args;

/**
 * Represents a literal string. Does not require any processing.
 */
public class StringLiteralArgument implements Argument {
    private final String value;
    private final ArgumentType type = ArgumentType.STRING_LITERAL;

    public StringLiteralArgument(String value) {
        this.value = value;
    }

    @Override
    public String processArgument() {
        return this.value;
    }

}
