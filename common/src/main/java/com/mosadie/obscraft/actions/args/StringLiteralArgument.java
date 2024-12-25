package com.mosadie.obscraft.actions.args;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;

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
