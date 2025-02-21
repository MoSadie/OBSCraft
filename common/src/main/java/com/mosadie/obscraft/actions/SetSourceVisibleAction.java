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

public class SetSourceVisibleAction extends ObsAction {

    public SetSourceVisibleAction(List<Argument> args, String obsId) {
        super(ActionType.SOURCE_VISIBILITY, args, obsId);
    }

    @Override
    public void execute() {
        try {
            OBSRemoteController obs = ObsCraft.getOBSIfReady(obsId);
            if (obs != null) {
                String scene = args.getFirst().processArgument();
                String source = args.get(1).processArgument();
                Visibility visiblity = Visibility.valueOf(args.get(2).processArgument().toUpperCase());

                obs.getSceneItemId(scene, source, 0, (response) -> {
                    if (response.isSuccessful()) {
                        obs.setSceneItemEnabled(scene, response.getSceneItemId(), visiblity.isVisible(), (response1) -> {
                            if (response1.isSuccessful()) {
                                LOGGER.info("[OBSCraft] Set source " + source + " in scene " + scene + " to " + (visiblity.isVisible() ? "visible" : "hidden") + " on OBS " + obsId);
                            } else {
                                LOGGER.info("[OBSCraft] Failed to set source " + source + " in scene " + scene + " to " + (visiblity.isVisible() ? "visible" : "hidden") + " on OBS " + obsId);
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("Error executing SetSourceVisibleAction", e);
        }
    }

    public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> GetCommand() {
        // setSourceVisible <obsId> scene literal/scoreboard [sceneId] source literal/scoreboard [sourceId] show/hide
        return ClientCommandRegistrationEvent.literal("setSourceVisible")
                .then(ClientCommandRegistrationEvent.argument("obsId", StringArgumentType.string())
                        .then(ClientCommandRegistrationEvent.literal("scene")
                                .then(ClientCommandRegistrationEvent.literal("literal")
                                        .then(ClientCommandRegistrationEvent.argument("scene", StringArgumentType.string())
                                                .then(ClientCommandRegistrationEvent.literal("source")
                                                        .then(ClientCommandRegistrationEvent.literal("literal")
                                                                .then(ClientCommandRegistrationEvent.argument("source", StringArgumentType.string())
                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.STRING_LITERAL, Visibility.SHOW)))
                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.STRING_LITERAL, Visibility.HIDE)))))
                                                        .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                                                .then(ClientCommandRegistrationEvent.argument("sourceObjective", ObjectiveArgument.objective())
                                                                        .then(ClientCommandRegistrationEvent.literal("byScore")
                                                                                .then(ClientCommandRegistrationEvent.argument("sourceScore", IntegerArgumentType.integer())
                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.SHOW)))
                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.HIDE)))))
                                                                        .then(ClientCommandRegistrationEvent.literal("byName")
                                                                                .then(ClientCommandRegistrationEvent.argument("sourceName", StringArgumentType.string())
                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.SHOW)))
                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.HIDE))))))))))
                                .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                        .then(ClientCommandRegistrationEvent.argument("sceneObjective", ObjectiveArgument.objective())
                                                .then(ClientCommandRegistrationEvent.literal("byScore")
                                                        .then(ClientCommandRegistrationEvent.argument("sceneScore", IntegerArgumentType.integer())
                                                                .then(ClientCommandRegistrationEvent.literal("source")
                                                                        .then(ClientCommandRegistrationEvent.literal("literal")
                                                                                .then(ClientCommandRegistrationEvent.argument("source", StringArgumentType.string())
                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.STRING_LITERAL, Visibility.SHOW)))
                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.STRING_LITERAL, Visibility.HIDE)))))
                                                                        .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                                                                .then(ClientCommandRegistrationEvent.argument("sourceObjective", ObjectiveArgument.objective())
                                                                                        .then(ClientCommandRegistrationEvent.literal("byScore")
                                                                                                .then(ClientCommandRegistrationEvent.argument("sourceScore", IntegerArgumentType.integer())
                                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.SHOW)))
                                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.HIDE)))))
                                                                                        .then(ClientCommandRegistrationEvent.literal("byName")
                                                                                                .then(ClientCommandRegistrationEvent.argument("sourceName", StringArgumentType.string())
                                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.SHOW)))
                                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.HIDE))))))))))
                                                .then(ClientCommandRegistrationEvent.literal("byName")
                                                        .then(ClientCommandRegistrationEvent.argument("sceneName", StringArgumentType.string())
                                                                .then(ClientCommandRegistrationEvent.literal("source")
                                                                        .then(ClientCommandRegistrationEvent.literal("literal")
                                                                                .then(ClientCommandRegistrationEvent.argument("source", StringArgumentType.string())
                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.STRING_LITERAL, Visibility.SHOW)))
                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.STRING_LITERAL, Visibility.HIDE)))))
                                                                        .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                                                                .then(ClientCommandRegistrationEvent.argument("sourceObjective", ObjectiveArgument.objective())
                                                                                        .then(ClientCommandRegistrationEvent.literal("byScore")
                                                                                                .then(ClientCommandRegistrationEvent.argument("sourceScore", IntegerArgumentType.integer())
                                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.SHOW)))
                                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_SCORE, Visibility.HIDE)))))
                                                                                        .then(ClientCommandRegistrationEvent.literal("byName")
                                                                                                .then(ClientCommandRegistrationEvent.argument("sourceName", StringArgumentType.string())
                                                                                                        .then(ClientCommandRegistrationEvent.literal("show")
                                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.SHOW)))
                                                                                                        .then(ClientCommandRegistrationEvent.literal("hide")
                                                                                                                .executes(context -> SetSourceVisibleExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Argument.ArgumentType.SCOREBOARD_WITH_NAME, Visibility.HIDE))))))))))))));
    }

    private static int SetSourceVisibleExecuteCommand(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, Argument.ArgumentType sceneArgType, Argument.ArgumentType sourceArgType, Visibility visibility) {
        String obsId = StringArgumentType.getString(context, "obsId");
        Argument sceneArg;
        Argument sourceArg;

        switch (sceneArgType) {
            case STRING_LITERAL ->
                    sceneArg = new StringLiteralArgument(StringArgumentType.getString(context, "scene"));
            case SCOREBOARD_WITH_SCORE ->
                    sceneArg = new ScoreboardWithScoreArgument(StringArgumentType.getString(context, "sceneObjective"), IntegerArgumentType.getInteger(context, "sceneScore"));
            case SCOREBOARD_WITH_NAME ->
                    sceneArg = new ScoreboardWithNameArgument(StringArgumentType.getString(context, "sceneObjective"), StringArgumentType.getString(context, "sceneName"));
            default -> sceneArg = new StringLiteralArgument("");
        }

        switch (sourceArgType) {
            case STRING_LITERAL ->
                    sourceArg = new StringLiteralArgument(StringArgumentType.getString(context, "source"));
            case SCOREBOARD_WITH_SCORE ->
                    sourceArg = new ScoreboardWithScoreArgument(StringArgumentType.getString(context, "sourceObjective"), IntegerArgumentType.getInteger(context, "sourceScore"));
            case SCOREBOARD_WITH_NAME ->
                    sourceArg = new ScoreboardWithNameArgument(StringArgumentType.getString(context, "sourceObjective"), StringArgumentType.getString(context, "sourceName"));
            default -> sourceArg = new StringLiteralArgument("");
        }

        if (readyConnections.containsKey(obsId) && readyConnections.get(obsId)) {
            OBSRemoteController obs = obsConnections.get(obsId);
            obs.getSceneItemId(sceneArg.processArgument(), sourceArg.processArgument(), 0, (response) -> {
                if (response.isSuccessful()) {
                    obs.setSceneItemEnabled(sceneArg.processArgument(), response.getSceneItemId(), visibility.isVisible(), (response1) -> {
                        if (response1.isSuccessful()) {
                            SetSourceVisibleAction action = new SetSourceVisibleAction(Arrays.asList(sceneArg, sourceArg, new StringLiteralArgument(visibility.name())), obsId);
                            LOGGER.info("[OBSCraft] Source Visible JSON: " + action.getTellRawComponent());
                            context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Set source " + sourceArg.processArgument() + " in scene " + sceneArg.processArgument() + " to " + (visibility.isVisible() ? "visible" : "hidden") + " on OBS " + obsId).withStyle(ChatFormatting.GREEN), false);
                            context.getSource().arch$sendSuccess(() -> Component.literal("[Click here to copy tellraw command]")
                            .withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GOLD)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/tellraw " + Minecraft.getInstance().getGameProfile().getName() + " " + action.getTellRawComponent()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy tellraw command!")))), false);
                        } else {
                            LOGGER.info("[OBSCraft] Failed to set source " + sourceArg.processArgument() + " in scene " + sceneArg.processArgument() + " to " + (visibility.isVisible() ? "visible" : "hidden") + " on OBS " + obsId);
                        }
                    });
                }
            });
            return 1;
        } else {
            context.getSource().arch$sendFailure(Component.literal("[OBSCraft] OBS " + obsId + " is not ready.").withStyle(ChatFormatting.RED));
            return 1;
        }
    }
}
