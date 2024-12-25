package com.mosadie.obscraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mosadie.obscraft.actions.*;
import com.mosadie.obscraft.actions.args.Argument;
import com.mosadie.obscraft.actions.args.ScoreboardWithNameArgument;
import com.mosadie.obscraft.actions.args.ScoreboardWithScoreArgument;
import com.mosadie.obscraft.actions.args.StringLiteralArgument;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.platform.Platform;
import io.obswebsocket.community.client.OBSRemoteController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public final class ObsCraft {
    public static final String MOD_ID = "obscraft";

    public static final String TRANSLATION_TRIGGER = "com.mosadie.obscraft.trigger";

    public static Gson GSON_PRETTY;

    public static Gson GSON_COMPRESSED;

    private static Config config;

    public static Logger LOGGER = LogManager.getLogger();

    public static Map<String, OBSRemoteController> obsConnections = new HashMap<>();
    public static Map<String, Boolean> readyConnections = new HashMap<>();

    public static void init() {
        GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
        GSON_COMPRESSED = new GsonBuilder().create();

        File configFile = Platform.getConfigFolder().resolve("obscraft.json").toFile();

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

        LOGGER.info("OBSCraft initialized.");
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
                        showConnectedToast(info);
                        readyConnections.put(info.ID, true);
                    }).onClose((webSocketCloseCode -> {
                        obsConnections.remove(info.ID);
                        readyConnections.put(info.ID, false);
                        LOGGER.info("Closed connection to to OBS " + info.ID + " due to: " + webSocketCloseCode.toString());
                        showDisconnectedToast(info);
                    })).onCommunicatorError((error) -> {
                        LOGGER.error("Error communicating with OBS " + info.ID + ": " + error.getReason());
                        readyConnections.put(info.ID, false);
                        showFailedToast(info);
                    }).onControllerError((error) -> {
                        LOGGER.error("Error controlling OBS " + info.ID + ": " + error.getReason());
                        readyConnections.put(info.ID, false);
                        showFailedToast(info);
                    }).and()
                    .build();

            obsConnections.put(info.ID, obs);
        }
        LOGGER.info("Refreshed obs connections.");
    }

    public static void closeObsConnection(String id) {
        if (obsConnections.containsKey(id)) {
            OBSRemoteController obs = obsConnections.get(id);
            obs.disconnect();
        }
    }

    public static void createObsConnection(Config.OBSConnectionInfo info) {
        if (obsConnections.containsKey(info.ID)) {
            closeObsConnection(info.ID);
            obsConnections.remove(info.ID);
        }

        OBSRemoteController obs = OBSRemoteController.builder()
                .host(info.host)
                .port(info.port)
                .password(info.password)
                .connectionTimeout(3)
                .autoConnect(true)
                .lifecycle().onReady(() -> {
                    LOGGER.info("Connected to OBS " + info.ID + " (" + info.host + ":" + info.port + ")");
                    showConnectedToast(info);
                    readyConnections.put(info.ID, true);
                }).onClose((webSocketCloseCode -> {
                    obsConnections.remove(info.ID);
                    readyConnections.put(info.ID, false);
                    LOGGER.info("Closed connection to to OBS " + info.ID + " due to: " + webSocketCloseCode.toString());
                    showDisconnectedToast(info);
                })).onCommunicatorError((error) -> {
                    LOGGER.error("Error communicating with OBS " + info.ID + ": " + error.getReason());
                    readyConnections.put(info.ID, false);
                    showFailedToast(info);
                }).onControllerError((error) -> {
                    LOGGER.error("Error controlling OBS " + info.ID + ": " + error.getReason());
                    readyConnections.put(info.ID, false);
                    showFailedToast(info);
                }).and()
                .build();

        obsConnections.put(info.ID, obs);
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

    private static void showConnectedToast(Config.OBSConnectionInfo info) {
        Minecraft.getInstance().doRunTask(() -> {
            if (Minecraft.getInstance().font != null)
                Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal("[OBSCraft] Connected to OBS " + info.ID).withStyle(ChatFormatting.GREEN), Component.literal("")));
        });
    }

    private static void showDisconnectedToast(Config.OBSConnectionInfo info) {
        Minecraft.getInstance().doRunTask(() -> {
            if (Minecraft.getInstance().font != null)
                Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal("[OBSCraft] Disconnected from OBS " + info.ID).withStyle(ChatFormatting.GREEN), Component.literal("Check the logs if this is unexpected. Use '/obscraft reconnect' to try again.")));
        });
    }

    private static void showFailedToast(Config.OBSConnectionInfo info) {
        Minecraft.getInstance().doRunTask(() -> {
            if (Minecraft.getInstance().font != null)
                Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal("[OBSCraft] Failed to connect to OBS " + info.ID).withStyle(ChatFormatting.GREEN), Component.literal("Check the logs if this is unexpected. Use '/obscraft reconnect' to try again.")));
        });
    }

    public static boolean handleTranslatableContent(TranslatableContents translatableContents) {
        // Handle a translatable content
        // If the translation key is the trigger, parse the action and do it

        if (translatableContents.getKey().equals(TRANSLATION_TRIGGER)) {
            try {
                String obsId = translatableContents.getArgs()[0].toString();
                ObsAction.ActionType actionType = ObsAction.ActionType.valueOf(translatableContents.getArgs()[1].toString());

                String[] args = Arrays.copyOfRange(translatableContents.getArgs(), 2, translatableContents.getArgs().length, String[].class);

                // Convert args to JsonObjects
                List<Argument> arguments = new ArrayList<>();
                for (String arg : args) {
                    // Make a JsonObject from the string
                    JsonObject argJson = GSON_COMPRESSED.fromJson(arg, JsonObject.class);

                    // Get the type of the argument
                    Argument.ArgumentType argType = Argument.ArgumentType.valueOf(argJson.get("type").getAsString());

                    // Create the argument object
                    Argument argument = null;
                    switch (argType) {
                        case STRING_LITERAL -> argument = new StringLiteralArgument(argJson.get("value").getAsString());
                        case SCOREBOARD_WITH_SCORE ->
                                argument = new ScoreboardWithScoreArgument(argJson.get("objective").getAsString(), argJson.get("score").getAsInt());
                        case SCOREBOARD_WITH_NAME ->
                                argument = new ScoreboardWithNameArgument(argJson.get("objective").getAsString(), argJson.get("scoreHolder").getAsString());
                    }

                    arguments.add(argument);
                }

                ObsAction action = null;

                switch (actionType) {
                    case SCENE -> {
                        action = new SetProgramSceneAction(arguments, obsId);
                    }

                    case FILTER -> {
                        action = new SetFilterAction(arguments, obsId);
                    }

                    case TEXT -> {
                        action = new SetTextAction(arguments, obsId);
                    }

                    case SOURCE_VISIBILITY -> {
                        action = new SetSourceVisibleAction(arguments, obsId);
                    }

                    default -> {
                        LOGGER.error("Unknown action type: " + actionType);
                        return false;
                    }
                }
                LOGGER.info("Triggered action: " + action.type);
                action.execute();
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

        return ClientCommandRegistrationEvent.literal("obscraft")
                .then(ClientCommandRegistrationEvent.literal("list").executes((context -> {
                    // List all available and ready OBS connections
                    context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Available OBS Connections:").withStyle(ChatFormatting.GREEN), false);
                    for (String id : getAvailableOBSIds()) {
                        context.getSource().arch$sendSuccess(() -> Component.literal("- " + id).withStyle(ChatFormatting.GRAY), false);
                    }

                    Set<String> unavailableIds = obsConnections.keySet().stream().filter(id -> !readyConnections.get(id)).collect(HashSet::new, HashSet::add, HashSet::addAll);

                    if (!unavailableIds.isEmpty()) {
                        context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Unavailable OBS Connections:").withStyle(ChatFormatting.RED), false);
                        for (String id : unavailableIds) {
                            context.getSource().arch$sendSuccess(() -> Component.literal("- " + id).withStyle(ChatFormatting.GRAY), false);
                        }
                    }
                    return 1;
                })))
                .then(ClientCommandRegistrationEvent.literal("reconnect")
                        .then(ClientCommandRegistrationEvent.argument("obs_id", StringArgumentType.string())
                                .executes((context -> {
                                    // Reconnect to a specific OBS connection
                                    String obsId = StringArgumentType.getString(context, "obs_id");
                                    Config.OBSConnectionInfo info = config.getConnectionById(obsId);
                                    if (info != null) {
                                        createObsConnection(config.getConnectionById(obsId));
                                        context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Reconnecting to OBS " + obsId).withStyle(ChatFormatting.GREEN), false);
                                    } else {
                                        context.getSource().arch$sendFailure(Component.literal("[OBSCraft] OBS " + obsId + " not found.").withStyle(ChatFormatting.RED));
                                    }
                                    return 1;
                                }))))
                .then(SetProgramSceneAction.GetCommand())
                .then(SetFilterAction.GetCommand())
                .then(SetTextAction.GetCommand())
                .then(SetSourceVisibleAction.GetCommand());
    }
}
