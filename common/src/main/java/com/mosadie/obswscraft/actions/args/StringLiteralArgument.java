package com.mosadie.obswscraft.actions.args;

/**
 * Represents a literal string. Does not require any processing.
 */
public class StringLiteralArgument implements Argument {
    private final String value;

    public StringLiteralArgument(String value) {
        this.value = value;
    }

    @Override
    public String processArgument() {
        return this.value;
    }
}
