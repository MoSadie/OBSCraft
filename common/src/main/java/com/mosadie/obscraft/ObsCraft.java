package com.mosadie.obscraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mosadie.obscraft.actions.*;
import com.mosadie.obscraft.actions.args.Argument;
import com.mosadie.obscraft.actions.args.ScoreboardWithNameArgument;
import com.mosadie.obscraft.actions.args.ScoreboardWithScoreArgument;
import com.mosadie.obscraft.actions.args.StringLiteralArgument;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.obswebsocket.community.client.OBSRemoteController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public final class ObsCraft {
    public static final String MOD_ID = "obscraft";

    public static final String TRANSLATION_TRIGGER = "com.mosadie.obscraft.trigger";

    // F6 - F10
    public static final KeyMapping ACTION_KEY_1 = new KeyMapping("key.obscraft.action1", 295, "key.categories.obscraft");
    public static final KeyMapping ACTION_KEY_2 = new KeyMapping("key.obscraft.action2", 296, "key.categories.obscraft");
    public static final KeyMapping ACTION_KEY_3 = new KeyMapping("key.obscraft.action3", 297, "key.categories.obscraft");
    public static final KeyMapping ACTION_KEY_4 = new KeyMapping("key.obscraft.action4", 298, "key.categories.obscraft");
    public static final KeyMapping ACTION_KEY_5 = new KeyMapping("key.obscraft.action5", 299, "key.categories.obscraft");

    // Unassigned by default
    public static final KeyMapping ACTION_KEY_6 = new KeyMapping("key.obscraft.action6", -1, "key.categories.obscraft");
    public static final KeyMapping ACTION_KEY_7 = new KeyMapping("key.obscraft.action7", -1, "key.categories.obscraft");
    public static final KeyMapping ACTION_KEY_8 = new KeyMapping("key.obscraft.action8", -1, "key.categories.obscraft");
    public static final KeyMapping ACTION_KEY_9 = new KeyMapping("key.obscraft.action9", -1, "key.categories.obscraft");

    public static final List<KeyMapping> ACTION_KEYS = List.of(ACTION_KEY_1, ACTION_KEY_2, ACTION_KEY_3, ACTION_KEY_4, ACTION_KEY_5, ACTION_KEY_6, ACTION_KEY_7, ACTION_KEY_8, ACTION_KEY_9);


    public static Gson GSON_PRETTY;

    public static Gson GSON_COMPRESSED;

    private static Config config;
    private static File configFile;

    public static Logger LOGGER = LogManager.getLogger();

    public static Map<String, OBSRemoteController> obsConnections = new HashMap<>();
    public static Map<String, Boolean> readyConnections = new HashMap<>();

    private static String toSaveActionName = null;

    public static void init() {
        GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
        GSON_COMPRESSED = new GsonBuilder().create();

        configFile = Platform.getConfigFolder().resolve("obscraft.json").toFile();

        config = Config.readOrDefault(configFile);

        obsConnections = new HashMap<>();
        readyConnections = new HashMap<>();

        refreshOBSConnections();

        ClientCommandRegistrationEvent.EVENT.register((dispatcher, context) -> {
            dispatcher.register(getCommand());
        });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (ACTION_KEY_1.consumeClick()) {
                ObsAction.ActionTranslatableComponent action = config.getSavedActionKey(1);
                if (action != null) {
                    handleTranslatableContent(action.toTranslatableContents());
                }
            }
        });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (ACTION_KEY_2.consumeClick()) {
                ObsAction.ActionTranslatableComponent action = config.getSavedActionKey(2);
                if (action != null) {
                    handleTranslatableContent(action.toTranslatableContents());
                }
            }
        });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (ACTION_KEY_3.consumeClick()) {
                ObsAction.ActionTranslatableComponent action = config.getSavedActionKey(3);
                if (action != null) {
                    handleTranslatableContent(action.toTranslatableContents());
                }
            }
        });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (ACTION_KEY_4.consumeClick()) {
                ObsAction.ActionTranslatableComponent action = config.getSavedActionKey(4);
                if (action != null) {
                    handleTranslatableContent(action.toTranslatableContents());
                }
            }
        });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (ACTION_KEY_5.consumeClick()) {
                ObsAction.ActionTranslatableComponent action = config.getSavedActionKey(5);
                if (action != null) {
                    handleTranslatableContent(action.toTranslatableContents());
                }
            }
        });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (ACTION_KEY_6.consumeClick()) {
                ObsAction.ActionTranslatableComponent action = config.getSavedActionKey(6);
                if (action != null) {
                    handleTranslatableContent(action.toTranslatableContents());
                }
            }
        });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (ACTION_KEY_7.consumeClick()) {
                ObsAction.ActionTranslatableComponent action = config.getSavedActionKey(7);
                if (action != null) {
                    handleTranslatableContent(action.toTranslatableContents());
                }
            }
        });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (ACTION_KEY_8.consumeClick()) {
                ObsAction.ActionTranslatableComponent action = config.getSavedActionKey(8);
                if (action != null) {
                    handleTranslatableContent(action.toTranslatableContents());
                }
            }
        });

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (ACTION_KEY_9.consumeClick()) {
                ObsAction.ActionTranslatableComponent action = config.getSavedActionKey(9);
                if (action != null) {
                    handleTranslatableContent(action.toTranslatableContents());
                }
            }
        });

        for (KeyMapping key : ACTION_KEYS) {
            KeyMappingRegistry.register(key);
        }

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
                Minecraft.getInstance().getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal("[OBSCraft] Connected to OBS " + info.ID).withStyle(ChatFormatting.GREEN), Component.literal("")));
        });
    }

    private static void showDisconnectedToast(Config.OBSConnectionInfo info) {
        Minecraft.getInstance().doRunTask(() -> {
            if (Minecraft.getInstance().font != null)
                Minecraft.getInstance().getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal("[OBSCraft] Disconnected from OBS " + info.ID).withStyle(ChatFormatting.GREEN), Component.literal("Check the logs if this is unexpected. Use '/obscraft reconnect' to try again.")));
        });
    }

    private static void showFailedToast(Config.OBSConnectionInfo info) {
        Minecraft.getInstance().doRunTask(() -> {
            if (Minecraft.getInstance().font != null)
                Minecraft.getInstance().getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal("[OBSCraft] Failed to connect to OBS " + info.ID).withStyle(ChatFormatting.GREEN), Component.literal("Check the logs if this is unexpected. Use '/obscraft reconnect' to try again.")));
        });
    }

    private static void showSavedToast(String actionId) {
        Minecraft.getInstance().doRunTask(() -> {
            if (Minecraft.getInstance().font != null)
                Minecraft.getInstance().getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal("[OBSCraft] Saved action with id:").withStyle(ChatFormatting.GREEN), Component.literal(actionId)));
        });
    }

    public static boolean isSaveEnabled() {
        return toSaveActionName != null;
    }

    public static void saveAction(ObsAction action) {
        if (toSaveActionName == null || action == null) {
            return;
        }

        String name = toSaveActionName;
        toSaveActionName = null;

        config.saveAction(name, action);
        config.save(configFile);

        LOGGER.info("Saving action as: {}", name);
        showSavedToast(name);
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

                if (isSaveEnabled()) {
                    saveAction(action);
                }

                LOGGER.info("Triggered action: " + action.type);
                action.execute();
                return true;
            } catch (Exception e) {
                LOGGER.error("Failed to parse translatable content.");
                LOGGER.error(e);
                LOGGER.catching(e);

            }
        }
        return false;
    }

    public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> getCommand() {
        // Create a Brigadier command that can be used in Minecraft

        return ClientCommandRegistrationEvent.literal("obscraft")
                .then(ClientCommandRegistrationEvent.literal("listConnections").executes((context -> {
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
                        .then(ClientCommandRegistrationEvent.argument("obsId", StringArgumentType.string())
                                .executes((context -> {
                                    // Reconnect to a specific OBS connection
                                    String obsId = StringArgumentType.getString(context, "obsId");
                                    Config.OBSConnectionInfo info = config.getConnectionById(obsId);
                                    if (info != null) {
                                        createObsConnection(config.getConnectionById(obsId));
                                        context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Reconnecting to OBS " + obsId).withStyle(ChatFormatting.GREEN), false);
                                    } else {
                                        context.getSource().arch$sendFailure(Component.literal("[OBSCraft] OBS " + obsId + " not found.").withStyle(ChatFormatting.RED));
                                    }
                                    return 1;
                                }))))
                .then(ClientCommandRegistrationEvent.literal("saveAction")
                        .then(ClientCommandRegistrationEvent.argument("actionId", StringArgumentType.string())
                                .executes((context -> {
                                    // Save an action to a specific key
                                    String id = StringArgumentType.getString(context, "actionId");
                                    toSaveActionName = id;
                                    context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Saving next action with id: " + id).withStyle(ChatFormatting.GREEN), false);
                                    return 1;
                                }))))
                .then(ClientCommandRegistrationEvent.literal("listSavedActions")
                        .executes((context -> {
                            if (config.savedActions == null || config.savedActions.isEmpty()) {
                                context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] No saved actions.").withStyle(ChatFormatting.GRAY), false);
                                return 1;
                            }

                            // List all saved actions
                            context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Saved Actions:").withStyle(ChatFormatting.GREEN), false);

                            for (String key : config.savedActions.keySet()) {
                                context.getSource().arch$sendSuccess(() -> Component.literal("- " + key).withStyle(ChatFormatting.GRAY), false);
                            }
                            return 1;
                        }))
                )
                .then(ClientCommandRegistrationEvent.literal("deleteSavedAction")
                        .then(ClientCommandRegistrationEvent.argument("actionId", StringArgumentType.string())
                                .executes((context -> {
                                    if (config.savedActions == null || config.savedActions.isEmpty()) {
                                        context.getSource().arch$sendFailure(Component.literal("[OBSCraft] No saved actions to delete.").withStyle(ChatFormatting.RED));
                                        return 1;
                                    }
                                    // Delete a saved action
                                    String id = StringArgumentType.getString(context, "actionId");
                                    if (config.savedActions.containsKey(id)) {
                                        config.savedActions.remove(id);
                                        if (config.savedActionKeys.containsValue(id)) {
                                            // Remove all keys that point to this action
                                            List<Integer> keysToRemove = new ArrayList<>();
                                            for (int key : config.savedActionKeys.keySet()) {
                                                if (config.savedActionKeys.get(key).equals(id)) {
                                                    keysToRemove.add(key);
                                                }
                                            }
                                            for (int key : keysToRemove) {
                                                config.savedActionKeys.remove(key);
                                            }
                                        }
                                        context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Deleted saved action with id: " + id).withStyle(ChatFormatting.GREEN), false);
                                        config.save(configFile);
                                    } else {
                                        context.getSource().arch$sendFailure(Component.literal("[OBSCraft] Saved action " + id + " not found.").withStyle(ChatFormatting.RED));
                                    }
                                    return 1;
                                })))
                )
                .then(ClientCommandRegistrationEvent.literal("doAction")
                        .then(ClientCommandRegistrationEvent.argument("actionId", StringArgumentType.string())
                                .executes((context -> {
                                    if (config.savedActions == null || config.savedActions.isEmpty()) {
                                        context.getSource().arch$sendFailure(Component.literal("[OBSCraft] No saved actions to use, make one first using /obscraft saveAction <id>").withStyle(ChatFormatting.RED));
                                        return 1;
                                    }
                                    // Do a saved action
                                    String actionId = StringArgumentType.getString(context, "actionId");
                                    if (config.savedActions.containsKey(actionId)) {
                                        ObsAction.ActionTranslatableComponent action = config.savedActions.get(actionId);
                                        handleTranslatableContent(action.toTranslatableContents());
                                    } else {
                                        context.getSource().arch$sendFailure(Component.literal("[OBSCraft] Saved action " + actionId + " not found.").withStyle(ChatFormatting.RED));
                                    }
                                    return 1;
                                }))))
                .then(ClientCommandRegistrationEvent.literal("setActionKey")
                        .then(ClientCommandRegistrationEvent.argument("key", IntegerArgumentType.integer(1, 10))
                                .then(ClientCommandRegistrationEvent.argument("id", StringArgumentType.string())
                                        .executes((context -> {
                                            if (config.savedActions == null || config.savedActions.isEmpty()) {
                                                context.getSource().arch$sendFailure(Component.literal("[OBSCraft] No saved actions to use, make one first using /obscraft saveAction <id>").withStyle(ChatFormatting.RED));
                                                return 1;
                                            }
                                            // Set a key to point to a saved action
                                            int key = IntegerArgumentType.getInteger(context, "key");
                                            String id = StringArgumentType.getString(context, "id");
                                            if (config.savedActions.containsKey(id)) {
                                                config.saveActionKey(key, id);
                                                context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Set action key " + key + " to saved action " + id).withStyle(ChatFormatting.GREEN), false);
                                                config.save(configFile);
                                            } else {
                                                context.getSource().arch$sendFailure(Component.literal("[OBSCraft] Saved action " + id + " not found.").withStyle(ChatFormatting.RED));
                                            }
                                            return 1;
                                        })))
                                .executes((context -> {
                                    if (config.savedActionKeys == null || config.savedActionKeys.isEmpty()) {
                                        context.getSource().arch$sendFailure(Component.literal("[OBSCraft] No saved action keys to clear.").withStyle(ChatFormatting.RED));
                                        return 1;
                                    }
                                    // Clear a key
                                    int key = IntegerArgumentType.getInteger(context, "key");
                                    if (config.savedActionKeys.containsKey(key)) {
                                        config.savedActionKeys.remove(key);
                                        context.getSource().arch$sendSuccess(() -> Component.literal("[OBSCraft] Cleared action key " + key).withStyle(ChatFormatting.GREEN), false);
                                        config.save(configFile);
                                    } else {
                                        context.getSource().arch$sendFailure(Component.literal("[OBSCraft] Action key " + key + " was already not set.").withStyle(ChatFormatting.RED));
                                    }
                                    return 1;
                                }))))
                .then(SetProgramSceneAction.GetCommand())
                .then(SetFilterAction.GetCommand())
                .then(SetTextAction.GetCommand())
                .then(SetSourceVisibleAction.GetCommand());
    }
}
