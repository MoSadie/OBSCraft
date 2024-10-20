package com.mosadie.obscraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mosadie.obscraft.actions.ObsAction;
import com.mosadie.obscraft.actions.SetProgramSceneAction;
import com.mosadie.obscraft.actions.args.Argument;
import com.mosadie.obscraft.actions.args.ScoreboardWithNameArgument;
import com.mosadie.obscraft.actions.args.ScoreboardWithScoreArgument;
import com.mosadie.obscraft.actions.args.StringLiteralArgument;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.platform.Platform;
import io.obswebsocket.community.client.OBSRemoteController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.http.HttpClient;
import java.util.*;

public final class ObsCraft {
    public static final String MOD_ID = "obscraft";

    public static final String TRANSLATION_TRIGGER = "com.mosadie.obscraft.trigger";

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
                        case SCOREBOARD_WITH_SCORE -> argument = new ScoreboardWithScoreArgument(argJson.get("objective").getAsString(), argJson.get("score").getAsInt());
                        case SCOREBOARD_WITH_NAME -> argument = new ScoreboardWithNameArgument(argJson.get("objective").getAsString(), argJson.get("scoreHolder").getAsString());
                    }

                    arguments.add(argument);
                }

                ObsAction action = null;

                switch (actionType) {
                    case SCENE -> {
                        action = new SetProgramSceneAction(arguments, obsId);
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
                    return 1;
                })))
                .then(ClientCommandRegistrationEvent.literal("set_scene")
                        .then(ClientCommandRegistrationEvent.argument("obs_id", StringArgumentType.string())
                                .then(ClientCommandRegistrationEvent.literal("literal")
                                        .then(ClientCommandRegistrationEvent.argument("scene", StringArgumentType.string())
                                                .executes(context -> SetProgramSceneCommand(context, Argument.ArgumentType.STRING_LITERAL))))
                                .then(ClientCommandRegistrationEvent.literal("scoreboard")
                                        .then(ClientCommandRegistrationEvent.argument("objective", ObjectiveArgument.objective())
                                                .then(ClientCommandRegistrationEvent.literal("by_score")
                                                        .then(ClientCommandRegistrationEvent.argument("score", IntegerArgumentType.integer())
                                                                .executes(context -> SetProgramSceneCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_SCORE))))
                                                .then(ClientCommandRegistrationEvent.literal("by_name")
                                                        .then(ClientCommandRegistrationEvent.argument("name", StringArgumentType.string())
                                                                .executes(context -> SetProgramSceneCommand(context, Argument.ArgumentType.SCOREBOARD_WITH_NAME))))))));
    }

    private static int SetProgramSceneCommand(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, Argument.ArgumentType argType) {
        String obsId = StringArgumentType.getString(context, "obs_id");
        Argument arg;

        switch (argType) {
            case STRING_LITERAL -> arg = new StringLiteralArgument(StringArgumentType.getString(context, "scene"));
            case SCOREBOARD_WITH_SCORE -> arg = new ScoreboardWithScoreArgument(StringArgumentType.getString(context, "objective"), IntegerArgumentType.getInteger(context, "score"));
            case SCOREBOARD_WITH_NAME -> arg = new ScoreboardWithNameArgument(StringArgumentType.getString(context, "objective"), StringArgumentType.getString(context, "name"));
            default -> arg = new StringLiteralArgument("");
        }

        if (readyConnections.containsKey(obsId) && readyConnections.get(obsId)) {
            OBSRemoteController obs = obsConnections.get(obsId);
            obs.setCurrentProgramScene(arg.processArgument(), (response) -> {
                if (response.isSuccessful()) {
                    SetProgramSceneAction action = new SetProgramSceneAction(Collections.singletonList(arg), obsId);
                    context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Set scene to " + arg.processArgument() + " on OBS " + obsId).withStyle(ChatFormatting.GREEN), false);
                    context.getSource().arch$sendSuccess(() -> Component.literal("[Click here to copy tellraw command]").withStyle(ChatFormatting.GOLD).withStyle(style -> {
                        style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/tellraw " + Minecraft.getInstance().getGameProfile().getName() + " " + action.getTellRawComponent()));
                        style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy tellraw command!")));
                        return style;
                    }), false);
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
