package com.mosadie.obscraft.actions;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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

public class SetTextAction extends ObsAction {

    public SetTextAction(List<Argument> args, String obsId) {
        super(ActionType.TEXT, args, obsId);
    }

    @Override
    public void execute() {
        try {
            OBSRemoteController obs = ObsCraft.getOBSIfReady(obsId);
            if (obs != null) {
                String source = args.getFirst().processArgument();
                String text = args.get(1).processArgument();

                JsonObject settings = new JsonObject();
                settings.add("text", new JsonPrimitive(text));

                obs.setInputSettings(source, settings, true, (response) -> {
                    if (response.isSuccessful()) {
                        ObsCraft.LOGGER.info("[OBSCraft] Set text source " + source + " to " + (text) + " on OBS " + obsId);
                    } else {
                        ObsCraft.LOGGER.info("[OBSCraft] Failed to set text source " + source + " to " + (text) + " on OBS " + obsId);
                    }
                });
            }
        } catch (Exception e) {
            ObsCraft.LOGGER.error("Error executing SetTextAction", e);
        }
    }

    public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> GetCommand() {
        return ClientCommandRegistrationEvent.literal("set_text")
                .then(ClientCommandRegistrationEvent.argument("obs_id", StringArgumentType.string())
                        .then(ClientCommandRegistrationEvent.literal("source")
                                .then(ClientCommandRegistrationEvent.literal("literal")
                                        .then(ClientCommandRegistrationEvent.argument("source", StringArgumentType.string())
                                                .then(ClientCommandRegistrationEvent.literal("text")
                                                        .then(ClientCommandRegistrationEvent.literal("literal")
                                                                .then(ClientCommandRegistrationEvent.argument("text", StringArgumentType.greedyString())
                                                                        .executes(context -> SetTextExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.STRING_LITERAL))))
                                                        .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                                                .then(ClientCommandRegistrationEvent.argument("text_objective", ObjectiveArgument.objective())
                                                                        .then(ClientCommandRegistrationEvent.literal("by_score")
                                                                                .then(ClientCommandRegistrationEvent.argument("text_score", IntegerArgumentType.integer())
                                                                                        .executes(context -> SetTextExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_SCORE))))
                                                                        .then(ClientCommandRegistrationEvent.literal("by_name")
                                                                                .then(ClientCommandRegistrationEvent.argument("text_name", StringArgumentType.string())
                                                                                        .executes(context -> SetTextExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_NAME)))))))))
                                .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                        .then(ClientCommandRegistrationEvent.argument("source_objective", ObjectiveArgument.objective())
                                                .then(ClientCommandRegistrationEvent.literal("by_score")
                                                        .then(ClientCommandRegistrationEvent.argument("source_score", IntegerArgumentType.integer())
                                                                .then(ClientCommandRegistrationEvent.literal("text")
                                                                        .then(ClientCommandRegistrationEvent.literal("literal")
                                                                                .then(ClientCommandRegistrationEvent.argument("text", StringArgumentType.greedyString())
                                                                                        .executes(context -> SetTextExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.STRING_LITERAL))))
                                                                        .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                                                                .then(ClientCommandRegistrationEvent.argument("text_objective", ObjectiveArgument.objective())
                                                                                        .then(ClientCommandRegistrationEvent.literal("by_score")
                                                                                                .then(ClientCommandRegistrationEvent.argument("text_score", IntegerArgumentType.integer())
                                                                                                        .executes(context -> SetTextExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_SCORE))))
                                                                                        .then(ClientCommandRegistrationEvent.literal("by_name")
                                                                                                .then(ClientCommandRegistrationEvent.argument("text_name", StringArgumentType.string())
                                                                                                        .executes(context -> SetTextExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_NAME)))))))))
                                                .then(ClientCommandRegistrationEvent.literal("by_name")
                                                        .then(ClientCommandRegistrationEvent.argument("source_name", StringArgumentType.string())
                                                                .then(ClientCommandRegistrationEvent.literal("text")
                                                                        .then(ClientCommandRegistrationEvent.literal("literal")
                                                                                .then(ClientCommandRegistrationEvent.argument("text", StringArgumentType.greedyString())
                                                                                        .executes(context -> SetTextExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.STRING_LITERAL))))
                                                                        .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                                                                .then(ClientCommandRegistrationEvent.argument("text_objective", ObjectiveArgument.objective())
                                                                                        .then(ClientCommandRegistrationEvent.literal("by_score")
                                                                                                .then(ClientCommandRegistrationEvent.argument("text_score", IntegerArgumentType.integer())
                                                                                                        .executes(context -> SetTextExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_SCORE))))
                                                                                        .then(ClientCommandRegistrationEvent.literal("by_name")
                                                                                                .then(ClientCommandRegistrationEvent.argument("text_name", StringArgumentType.string())
                                                                                                        .executes(context -> SetTextExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_NAME)))))))))))));
    }

    private static int SetTextExecuteCommand(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, Argument.ArgumentType sourceArgType, Argument.ArgumentType textArgType) {
        String obsId = StringArgumentType.getString(context, "obs_id");
        Argument sourceArg;
        Argument textArg;

        switch (sourceArgType) {
            case STRING_LITERAL -> sourceArg = new StringLiteralArgument(StringArgumentType.getString(context, "source"));
            case SCOREBOARD_WITH_SCORE -> sourceArg = new ScoreboardWithScoreArgument(StringArgumentType.getString(context, "source_objective"), IntegerArgumentType.getInteger(context, "source_score"));
            case SCOREBOARD_WITH_NAME -> sourceArg = new ScoreboardWithNameArgument(StringArgumentType.getString(context, "source_objective"), StringArgumentType.getString(context, "source_name"));
            default -> sourceArg = new StringLiteralArgument("");
        }

        switch (textArgType) {
            case STRING_LITERAL -> textArg = new StringLiteralArgument(StringArgumentType.getString(context, "text"));
            case SCOREBOARD_WITH_SCORE -> textArg = new ScoreboardWithScoreArgument(StringArgumentType.getString(context, "text_objective"), IntegerArgumentType.getInteger(context, "text_score"));
            case SCOREBOARD_WITH_NAME -> textArg = new ScoreboardWithNameArgument(StringArgumentType.getString(context, "text_objective"), StringArgumentType.getString(context, "text_name"));
            default -> textArg = new StringLiteralArgument("");
        }

        if (readyConnections.containsKey(obsId) && readyConnections.get(obsId)) {
            OBSRemoteController obs = obsConnections.get(obsId);

            JsonObject settings = new JsonObject();
            settings.add("text", new JsonPrimitive(textArg.processArgument()));

            obs.setInputSettings(sourceArg.processArgument(), settings, true, (response) -> {
                if (response.isSuccessful()) {
                    SetTextAction action = new SetTextAction(Arrays.asList(sourceArg, textArg), obsId);
                    LOGGER.info("[OBSCraft] Text JSON: " + action.getTellRawComponent());
                    context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Set text source " + sourceArg.processArgument() + " to " + (textArg.processArgument()) + " on OBS " + obsId).withStyle(ChatFormatting.GREEN), false);
                    context.getSource().arch$sendSuccess(() -> Component.literal("[Click here to copy tellraw command]")
                            .withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GOLD)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/tellraw " + Minecraft.getInstance().getGameProfile().getName() + " " + action.getTellRawComponent()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy tellraw command!")))), false);
                } else {
                    context.getSource().arch$sendFailure(Component.literal("[OBSCraft] Failed to set text source " + sourceArg.processArgument() + " to " + (textArg.processArgument()) + " on OBS " + obsId).withStyle(ChatFormatting.RED));
                }
            });
            return 1;
        } else {
            context.getSource().arch$sendFailure(Component.literal("[OBSCraft] OBS " + obsId + " is not ready.").withStyle(ChatFormatting.RED));
            return 1;
        }
    }
}
