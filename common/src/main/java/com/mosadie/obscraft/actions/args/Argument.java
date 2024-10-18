package com.mosadie.obscraft.actions.args;

public interface Argument {
    String processArgument();

    enum ArgumentType {
        STRING_LITERAL,
        SCOREBOARD_WITH_SCORE,
        SCOREBOARD_WITH_NAME
    }
}
