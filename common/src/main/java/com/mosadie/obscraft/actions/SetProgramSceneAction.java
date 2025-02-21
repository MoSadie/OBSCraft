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

import java.util.Collections;
import java.util.List;

import static com.mosadie.obscraft.ObsCraft.obsConnections;
import static com.mosadie.obscraft.ObsCraft.readyConnections;

public class SetProgramSceneAction extends ObsAction {

    // Args:
    // 0: sceneName

    public SetProgramSceneAction(List<Argument> args, String obsId) {
        super(ActionType.SCENE, args, obsId);
    }

    @Override
    public void execute() {
        try {
            OBSRemoteController obs = ObsCraft.getOBSIfReady(obsId);
            if (obs != null) {
                String scene = args.getFirst().processArgument();
                obs.setCurrentProgramScene(scene, (response) -> {
                    if (response.isSuccessful()) {
                        ObsCraft.LOGGER.info("[OBSCraft] Set scene to " + scene + " on OBS " + obsId);
                    } else {
                        ObsCraft.LOGGER.info("[OBSCraft] Failed to set scene to " + scene + " on OBS " + obsId);
                    }
                });
            }
        } catch (Exception e) {
            ObsCraft.LOGGER.error("Error executing SetProgramSceneAction", e);
        }
    }

    public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> GetCommand() {
        // setScene <obsId> literal/scoreboard [sceneId]
        return ClientCommandRegistrationEvent.literal("setScene")
                .then(ClientCommandRegistrationEvent.argument("obsId", StringArgumentType.string())
                        .then(ClientCommandRegistrationEvent.literal("scene")
                                .then(ClientCommandRegistrationEvent.literal("literal")
                                        .then(ClientCommandRegistrationEvent.argument("scene", StringArgumentType.string())
                                                .executes(context -> SetProgramSceneExecuteCommand(context, Argument.ArgumentType.STRING_LITERAL))))
                                .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                        .then(ClientCommandRegistrationEvent.argument("objective", ObjectiveArgument.objective())
                                                .then(ClientCommandRegistrationEvent.literal("byScore")
                                                        .then(ClientCommandRegistrationEvent.argument("score", IntegerArgumentType.integer())
                                                                .executes(context -> SetProgramSceneExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE))))
                                                .then(ClientCommandRegistrationEvent.literal("byName")
                                                        .then(ClientCommandRegistrationEvent.argument("name", StringArgumentType.string())
                                                                .executes(context -> SetProgramSceneExecuteCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME))))))));
    }

    private static int SetProgramSceneExecuteCommand(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, Argument.ArgumentType argType) {
        String obsId = StringArgumentType.getString(context, "obsId");
        Argument arg;

        switch (argType) {
            case STRING_LITERAL -> arg = new StringLiteralArgument(StringArgumentType.getString(context, "scene"));
            case SCOREBOARD_WITH_SCORE ->
                    arg = new ScoreboardWithScoreArgument(StringArgumentType.getString(context, "objective"), IntegerArgumentType.getInteger(context, "score"));
            case SCOREBOARD_WITH_NAME ->
                    arg = new ScoreboardWithNameArgument(StringArgumentType.getString(context, "objective"), StringArgumentType.getString(context, "name"));
            default -> arg = new StringLiteralArgument("");
        }

        if (readyConnections.containsKey(obsId) && readyConnections.get(obsId)) {
            OBSRemoteController obs = obsConnections.get(obsId);
            obs.setCurrentProgramScene(arg.processArgument(), (response) -> {
                if (response.isSuccessful()) {
                    SetProgramSceneAction action = new SetProgramSceneAction(Collections.singletonList(arg), obsId);
                    context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Set scene to " + arg.processArgument() + " on OBS " + obsId).withStyle(ChatFormatting.GREEN), false);
                    context.getSource().arch$sendSuccess(() -> Component.literal("[Click here to copy tellraw command]")
                            .withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GOLD)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/tellraw " + Minecraft.getInstance().getGameProfile().getName() + " " + action.getTellRawComponent()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy tellraw command!")))), false);
                } else {
                    context.getSource().arch$sendFailure(Component.literal("[OBSCraft] Failed to set scene to " + arg.processArgument() + " on OBS " + obsId).withStyle(ChatFormatting.RED));
                }
            });
            return 1;
        } else {
            context.getSource().arch$sendFailure(Component.literal("[OBSCraft] OBS " + obsId + " is not ready.").withStyle(ChatFormatting.RED));
            return 1;
        }
    }
}
