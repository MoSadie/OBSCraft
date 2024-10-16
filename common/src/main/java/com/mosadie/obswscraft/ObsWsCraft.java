package com.mosadie.obswscraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mosadie.obswscraft.actions.ObsAction;
import com.mosadie.obswscraft.actions.SetProgramSceneAction;
import com.mosadie.obswscraft.actions.args.Argument;
import com.mosadie.obswscraft.actions.args.ScoreboardWithNameArgument;
import com.mosadie.obswscraft.actions.args.ScoreboardWithScoreArgument;
import com.mosadie.obswscraft.actions.args.StringLiteralArgument;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.platform.Platform;
import io.obswebsocket.community.client.OBSRemoteController;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.http.HttpClient;
import java.util.*;

public final class ObsWsCraft {
    public static final String MOD_ID = "obswscraft";

    public static final String TRANSLATION_TRIGGER = "com.mosadie.obswscraft.trigger";

    public static Gson GSON_PRETTY;

    public static Gson GSON_COMPRESSED;

    private static HttpClient httpClient;

    private static Config config;

    public static Logger LOGGER = LogManager.getLogger();

    public static Map<String, OBSRemoteController> obsConnections = new HashMap<>();
    public static Map<String, Boolean> readyConnections = new HashMap<>();

    public static void init() {
        GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
        GSON_COMPRESSED = new GsonBuilder().create();
        httpClient = HttpClient.newHttpClient();

        File configFile = Platform.getConfigFolder().resolve("obswscraft.json").toFile();

        if (configFile.exists()) {
            try {
                FileReader reader = new FileReader(configFile);
                config = GSON_PRETTY.fromJson(reader, Config.class);
            } catch (FileNotFoundException e) {
                LOGGER.error("Failed to find config file. Using default config.");
                config = Config.defaultConfig();
            } catch (Exception e) {
                LOGGER.error("Failed to read config file. Using default config.");
                config = Config.defaultConfig();
            }
        } else {
            config = Config.defaultConfig();
            try {
                if (configFile.createNewFile()) {
                    LOGGER.info("Created config file.");
                    // Write default config to file
                    FileWriter writer = new FileWriter(configFile);
                    GSON_PRETTY.toJson(config, writer);
                    writer.close();
                } else {
                    LOGGER.error("Failed to create default config file.");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to create config file.");
                LOGGER.error(e);
            }
        }

        obsConnections = new HashMap<>();
        readyConnections = new HashMap<>();

        refreshOBSConnections();

        ClientCommandRegistrationEvent.EVENT.register((dispatcher, context) -> {
            dispatcher.register(getCommand());
        });

        LOGGER.info("OBSWSCraft initialized.");
    }

    private static void closeAllOBSConnections() {
        // Close out all connections
        for (String id : obsConnections.keySet()) {
            OBSRemoteController obs = obsConnections.get(id);

            obs.disconnect();
        }
    }

    private static void refreshOBSConnections() {
        closeAllOBSConnections();

        obsConnections = new HashMap<>();
        readyConnections = new HashMap<>();

        for (Config.OBSConnectionInfo info : config.connections) {
            OBSRemoteController obs = OBSRemoteController.builder()
                    .host(info.host)
                    .port(info.port)
                    .password(info.password)
                    .connectionTimeout(3)
                    .autoConnect(true)
                    .lifecycle().onReady(() -> {
                        LOGGER.info("Connected to OBS " + info.ID + " (" + info.host + ":" + info.port + ")");
                        readyConnections.put(info.ID, true);
                    }).onClose((webSocketCloseCode -> {
                        obsConnections.remove(info.ID);
                        readyConnections.put(info.ID, false);
                        LOGGER.info("Connection to OBS " + info.ID + " closed: " + webSocketCloseCode.toString());
                    })).and()
                    .build();

            obsConnections.put(info.ID, obs);
        }
        LOGGER.info("Refreshed obs connections.");
    }

    public static Set<String> getAvailableOBSIds() {
        return obsConnections.keySet().stream().filter(id -> readyConnections.get(id)).collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    public static OBSRemoteController getOBSIfReady(String id) {
        if (readyConnections.containsKey(id) && readyConnections.get(id)) {
            return obsConnections.get(id);
        }
        return null;
    }

    public static boolean handleTranslatableContent(TranslatableContents translatableContents) {
        // Handle a translatable content
        // If the translation key is the trigger, parse the action and do it

        if (translatableContents.getKey().equals(TRANSLATION_TRIGGER)) {
            try {
                ObsAction action = GSON_COMPRESSED.fromJson(translatableContents.getArgs()[0].toString(), ObsAction.class);
                action.execute();
                LOGGER.info("Triggered action: " + action.type);
                return true;
            } catch (Exception e) {
                LOGGER.error("Failed to parse translatable content.");
                LOGGER.error(e);

            }
        }
        return false;
    }

    public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> getCommand() {
        // Create a Brigadier command that can be used in Minecraft

        return ClientCommandRegistrationEvent.literal("obswscraft")
                .then(ClientCommandRegistrationEvent.literal("list").executes((context -> {
                    // List all available and ready OBS connections
                    context.getSource().arch$sendSuccess(() -> Component.literal("[OBSWSCraft] Available OBS Connections:").withStyle(ChatFormatting.GREEN), false);
                    for (String id : getAvailableOBSIds()) {
                        context.getSource().arch$sendSuccess(() -> Component.literal("- " + id).withStyle(ChatFormatting.GRAY), false);
                    }
                    return 1;
                })))
                .then(ClientCommandRegistrationEvent.literal("set_scene")
                .then(ClientCommandRegistrationEvent.literal("literal")
                        .then(ClientCommandRegistrationEvent.argument("obs_id", StringArgumentType.string())
                                .then(ClientCommandRegistrationEvent.argument("scene", StringArgumentType.string())
                                        .executes(context -> SetProgramSceneCommand(context, Argument.ArgumentType.STRING_LITERAL)))))
                .then(ClientCommandRegistrationEvent.literal("scoreboard")
                        .then(ClientCommandRegistrationEvent.argument("objective", ObjectiveArgument.objective())
                                .then(ClientCommandRegistrationEvent.argument("by_score", IntegerArgumentType.integer())
                                        .executes(context -> SetProgramSceneCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE)))
                                .then(ClientCommandRegistrationEvent.argument("by_name", StringArgumentType.string())
                                        .executes(context -> SetProgramSceneCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME))))));
    }

    private static int SetProgramSceneCommand(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, Argument.ArgumentType argType) {
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
                    context.getSource().arch$sendSuccess(() -> Component.literal("[OBSWSCraft] Set scene to " + arg.processArgument() + " on OBS " + obsId).withStyle(ChatFormatting.GREEN), false);
                    context.getSource().arch$sendSuccess(() -> Component.literal("[Click here to copy tellraw command]").withStyle(ChatFormatting.GOLD).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/tellraw @s " + action.getTellRawComponent()))), false);
                } else {
                    context.getSource().arch$sendFailure(Component.literal("[OBSWSCraft] Failed to set scene to " + arg.processArgument() + " on OBS " + obsId).withStyle(ChatFormatting.RED));
                }
            });
            return 1;
        } else {
            context.getSource().arch$sendFailure(Component.literal("[OBSWSCraft] OBS " + obsId + " is not ready.").withStyle(ChatFormatting.RED));
            return 1;
        }
    }
}
