package com.mosadie.obswscraft.fabric.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mosadie.obswscraft.ObsWsCraft;
import com.mosadie.obswscraft.actions.SetProgramSceneAction;
import com.mosadie.obswscraft.actions.args.Argument;
import com.mosadie.obswscraft.actions.args.ScoreboardWithNameArgument;
import com.mosadie.obswscraft.actions.args.ScoreboardWithScoreArgument;
import com.mosadie.obswscraft.actions.args.StringLiteralArgument;
import io.obswebsocket.community.client.OBSRemoteController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.Collections;

import static com.mosadie.obswscraft.ObsWsCraft.*;

public final class ObsWsCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ObsWsCraft.init();

        ClientCommandRegistrationCallback.EVENT.register(this::registerClientCommand);
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::handleGameMessage);
    }

    private boolean handleGameMessage(Component component, boolean b) {
        if (component.getContents() instanceof TranslatableContents translatableContents) {
            return !handleTranslatableContent(translatableContents);
        }

        return true;
    }

    public void registerClientCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext commandBuildContext) {
        // Create a Brigadier command that can be used in Minecraft
        // The command should list all available actions and allow the player to do an action

        dispatcher.register(ClientCommandManager.literal("obswscraft")
                .then(ClientCommandManager.literal("list").executes((context -> {
                    // List all available and ready OBS connections
                    context.getSource().sendFeedback(Component.literal("[OBSWSCraft] Available OBS Connections:").withStyle(ChatFormatting.GREEN));
                    for (String id : getAvailableOBSIds()) {
                        context.getSource().sendFeedback(Component.literal("- " + id).withStyle(ChatFormatting.GRAY));
                    }
                    return 1;
                })))
                .then(ClientCommandManager.literal("set_scene")
                        .then(ClientCommandManager.literal("literal")
                                .then(ClientCommandManager.argument("obs_id", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("scene", StringArgumentType.string())
                                                .executes(context -> SetProgramSceneCommand(context, Argument.ArgumentType.STRING_LITERAL)))))
                        .then(ClientCommandManager.literal("scoreboard")
                                .then(ClientCommandManager.argument("objective", ObjectiveArgument.objective())
                                        .then(ClientCommandManager.argument("by_score", IntegerArgumentType.integer())
                                                .executes(context -> SetProgramSceneCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE)))
                                        .then(ClientCommandManager.argument("by_name", StringArgumentType.string())
                                                .executes(context -> SetProgramSceneCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME)))))));
    }

    private static int SetProgramSceneCommand(CommandContext<FabricClientCommandSource> context, Argument.ArgumentType argType) {
        String obsId = StringArgumentType.getString(context, "obs_id");
        Argument arg;

        switch (argType) {
            case STRING_LITERAL -> arg = new StringLiteralArgument(StringArgumentType.getString(context, "scene"));
            case SCOREBOARD_WITH_SCORE -> arg = new ScoreboardWithScoreArgument(StringArgumentType.getString(context, "objective"), IntegerArgumentType.getInteger(context, "by_score"));
            case SCOREBOARD_WITH_NAME -> arg = new ScoreboardWithNameArgument(StringArgumentType.getString(context, "objective"), StringArgumentType.getString(context, "by_name"));
            default -> arg = new StringLiteralArgument("");
        }

        if (readyConnections.containsKey(obsId) && readyConnections.get(obsId)) {
            OBSRemoteController obs = obsConnections.get(obsId);
            obs.setCurrentProgramScene(arg.processArgument(), (response) -> {
                if (response.isSuccessful()) {
                    SetProgramSceneAction action = new SetProgramSceneAction(Collections.singletonList(arg), obsId);
                    context.getSource().sendFeedback(Component.literal("[OBSWSCraft] Set scene to " + arg.processArgument() + " on OBS " + obsId).withStyle(ChatFormatting.GREEN));
                    context.getSource().sendFeedback(Component.literal("[Click here to copy tellraw command]").withStyle(ChatFormatting.GOLD).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/tellraw @s " + action.getTellRawComponent()))));
                } else {
                    context.getSource().sendFeedback(Component.literal("[OBSWSCraft] Failed to set scene to " + arg.processArgument() + " on OBS " + obsId).withStyle(ChatFormatting.RED));
                }
            });
            return 1;
        } else {
            context.getSource().sendFeedback(Component.literal("[OBSWSCraft] OBS " + obsId + " is not ready.").withStyle(ChatFormatting.RED));
            return 1;
        }
    }
}
