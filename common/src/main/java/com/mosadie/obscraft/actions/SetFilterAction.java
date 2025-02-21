package com.mosadie.obscraft.actions;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mosadie.obscraft.ObsCraft;
import com.mosadie.obscraft.actions.args.Argument;
import com.mosadie.obscraft.actions.args.ScoreboardWithNameArgument;
import com.mosadie.obscraft.actions.args.ScoreboardWithScoreArgument;
import com.mosadie.obscraft.actions.args.StringLiteralArgument;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import io.obswebsocket.community.client.OBSRemoteController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.List;

import static com.mosadie.obscraft.ObsCraft.*;

public class SetFilterAction extends ObsAction {

    public SetFilterAction(List<Argument> args, String obsId) {
        super(ActionType.FILTER, args, obsId);
    }

    @Override
    public void execute() {
        try {
            OBSRemoteController obs = ObsCraft.getOBSIfReady(obsId);
            if (obs != null) {
                String source = args.getFirst().processArgument();
                String filter = args.get(1).processArgument();
                Visibility visiblity = Visibility.valueOf(args.get(2).processArgument().toUpperCase());

                obs.setSourceFilterEnabled(source, filter, visiblity.isVisible(), (response) -> {
                    if (response.isSuccessful()) {
                        LOGGER.info("[OBSCraft] Set filter " + filter + " on " + source + " to " + (visiblity) + " on OBS " + obsId);
                    } else {
                        LOGGER.info("[OBSCraft] Failed to set filter " + filter + " on " + source + " to " + (visiblity) + " on OBS " + obsId);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("Error executing SetFilterAction", e);
        }
    }

    public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> GetCommand() {
        // setFilter <obsId> literal/scoreboard [sourceId] literal/scoreboard [filterId] show/hide
        return ClientCommandRegistrationEvent.literal("setFilter")
                .then(ClientCommandRegistrationEvent.argument("obsId", StringArgumentType.string())
                        .then(ClientCommandRegistrationEvent.literal("source")
                                .then(ClientCommandRegistrationEvent.literal("literal")
                                        .then(ClientCommandRegistrationEvent.argument("source", StringArgumentType.string())
                                                .then(ClientCommandRegistrationEvent.literal("filter")
                                                        .then(ClientCommandRegistrationEvent.literal("literal")
                                                                .then(ClientCommandRegistrationEvent.argument("filter", StringArgumentType.string())
                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.STRING_LITERAL, Visibility.SHOW)))
                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.STRING_LITERAL, Visibility.HIDE)))))
                                                        .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                                                .then(ClientCommandRegistrationEvent.argument("filterObjective", ObjectiveArgument.objective())
                                                                        .then(ClientCommandRegistrationEvent.literal("byScore")
                                                                                .then(ClientCommandRegistrationEvent.argument("filterScore", IntegerArgumentType.integer())
                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.SHOW)))
                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.HIDE)))))
                                                                        .then(ClientCommandRegistrationEvent.literal("byName")
                                                                                .then(ClientCommandRegistrationEvent.argument("filterName", StringArgumentType.string())
                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.SHOW)))
                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.HIDE))))))))))
                                .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                        .then(ClientCommandRegistrationEvent.argument("sourceObjective", ObjectiveArgument.objective())
                                                .then(ClientCommandRegistrationEvent.literal("byScore")
                                                        .then(ClientCommandRegistrationEvent.argument("sourceScore", IntegerArgumentType.integer())
                                                                .then(ClientCommandRegistrationEvent.literal("filter")
                                                                        .then(ClientCommandRegistrationEvent.literal("literal")
                                                                                .then(ClientCommandRegistrationEvent.argument("filter", StringArgumentType.string())
                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.STRING_LITERAL, Visibility.SHOW)))
                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.STRING_LITERAL, Visibility.HIDE)))))
                                                                        .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                                                                .then(ClientCommandRegistrationEvent.argument("filterObjective", ObjectiveArgument.objective())
                                                                                        .then(ClientCommandRegistrationEvent.literal("byScore")
                                                                                                .then(ClientCommandRegistrationEvent.argument("filterScore", IntegerArgumentType.integer())
                                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.SHOW)))
                                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.HIDE)))))
                                                                                        .then(ClientCommandRegistrationEvent.literal("byName")
                                                                                                .then(ClientCommandRegistrationEvent.argument("filterName", StringArgumentType.string())
                                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.SHOW)))
                                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.HIDE))))))))))
                                                .then(ClientCommandRegistrationEvent.literal("byName")
                                                        .then(ClientCommandRegistrationEvent.argument("sourceName", StringArgumentType.string())
                                                                .then(ClientCommandRegistrationEvent.literal("filter")
                                                                        .then(ClientCommandRegistrationEvent.literal("literal")
                                                                                .then(ClientCommandRegistrationEvent.argument("filter", StringArgumentType.string())
                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.STRING_LITERAL, Visibility.SHOW)))
                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.STRING_LITERAL, Visibility.HIDE)))))
                                                                        .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                                                                .then(ClientCommandRegistrationEvent.argument("filterObjective", ObjectiveArgument.objective())
                                                                                        .then(ClientCommandRegistrationEvent.literal("byScore")
                                                                                                .then(ClientCommandRegistrationEvent.argument("filterScore", IntegerArgumentType.integer())
                                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.SHOW)))
                                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.HIDE)))))
                                                                                        .then(ClientCommandRegistrationEvent.literal("byName")
                                                                                                .then(ClientCommandRegistrationEvent.argument("filterName", StringArgumentType.string())
                                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.SHOW)))
                                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                                .executes(context -> SetFilterExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.HIDE))))))))))))));
    }

    private static int SetFilterExecuteCommand(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, Argument.ArgumentType sourceArgType, Argument.ArgumentType filterArgType, Visibility visibility) {
        String obsId = StringArgumentType.getString(context, "obsId");
        Argument sourceArg;
        Argument filterArg;

        switch (sourceArgType) {
            case STRING_LITERAL ->
                    sourceArg = new StringLiteralArgument(StringArgumentType.getString(context, "source"));
            case SCOREBOARD_WITH_SCORE ->
                    sourceArg = new ScoreboardWithScoreArgument(StringArgumentType.getString(context, "sourceObjective"), IntegerArgumentType.getInteger(context, "sourceScore"));
            case SCOREBOARD_WITH_NAME ->
                    sourceArg = new ScoreboardWithNameArgument(StringArgumentType.getString(context, "sourceObjective"), StringArgumentType.getString(context, "sourceName"));
            default -> sourceArg = new StringLiteralArgument("");
        }

        switch (filterArgType) {
            case STRING_LITERAL ->
                    filterArg = new StringLiteralArgument(StringArgumentType.getString(context, "filter"));
            case SCOREBOARD_WITH_SCORE ->
                    filterArg = new ScoreboardWithScoreArgument(StringArgumentType.getString(context, "filterObjective"), IntegerArgumentType.getInteger(context, "filterScore"));
            case SCOREBOARD_WITH_NAME ->
                    filterArg = new ScoreboardWithNameArgument(StringArgumentType.getString(context, "filterObjective"), StringArgumentType.getString(context, "filterName"));
            default -> filterArg = new StringLiteralArgument("");
        }

        if (readyConnections.containsKey(obsId) && readyConnections.get(obsId)) {
            OBSRemoteController obs = obsConnections.get(obsId);
            obs.setSourceFilterEnabled(sourceArg.processArgument(), filterArg.processArgument(), visibility.isVisible(), (response) -> {
                if (response.isSuccessful()) {
                    SetFilterAction action = new SetFilterAction(Arrays.asList(sourceArg, filterArg, new StringLiteralArgument(visibility.name())), obsId);
                    LOGGER.info("[OBSCraft] Filter JSON: " + action.getTellRawComponent());
                    context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Set filter " + filterArg.processArgument() + " to " + (visibility.isVisible() ? "visible" : "hidden") + " on source " + sourceArg.processArgument() + " on OBS " + obsId).withStyle(ChatFormatting.GREEN), false);
                    context.getSource().arch$sendSuccess(() -> Component.literal("[Click here to copy tellraw command]")
                            .withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GOLD)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/tellraw " + Minecraft.getInstance().getGameProfile().getName() + " " + action.getTellRawComponent()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy tellraw command!")))), false);
                } else {
                    context.getSource().arch$sendFailure(Component.literal("[OBSCraft] Failed to set filter " + filterArg.processArgument() + " to " + (visibility.isVisible() ? "visible" : "hidden") + " on source " + sourceArg.processArgument() + " on OBS " + obsId).withStyle(ChatFormatting.RED));
                }
            });
            return 1;
        } else {
            context.getSource().arch$sendFailure(Component.literal("[OBSCraft] OBS " + obsId + " is not ready.").withStyle(ChatFormatting.RED));
            return 1;
        }
    }
}
