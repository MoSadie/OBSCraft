package com.mosadie.obscraft;

import com.mosadie.obscraft.actions.ObsAction;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mosadie.obscraft.ObsCraft.GSON_PRETTY;
import static com.mosadie.obscraft.ObsCraft.LOGGER;

public class Config {
    public List<OBSConnectionInfo> connections;
    public Map<String, ObsAction.ActionTranslatableComponent> savedActions;
    public Map<Integer, String> savedActionKeys;

    public static Config readOrDefault(File configFile) {
        Config config;
        if (configFile.exists()) {
            config = new Config();
            config.load(configFile);
        } else {
            config = defaultConfig();
            config.save(configFile);
        }
        return config;
    }

    public static Config defaultConfig() {
        Config config = new Config();
        OBSConnectionInfo defaultObs = new OBSConnectionInfo();
        defaultObs.ID = "default";
        defaultObs.host = "127.0.0.1";
        defaultObs.port = 4455;
        defaultObs.password = "password";

        config.connections = new ArrayList<>();
        config.connections.add(defaultObs);

        config.savedActions = new HashMap<>();
        config.savedActionKeys = new HashMap<>();
        return config;
    }

    public void save(File configFile) {
        // Save the config to the file
        try {
            if (!configFile.exists()) {
                boolean configCreated = configFile.createNewFile();
                if (configCreated)
                    LOGGER.warn("Config file did not exist, creating new file. Make sure to update the config file with your settings!");
                else {
                    LOGGER.error("Failed to create config file. Something is very wrong.");
                    return;
                }
            }

            FileWriter writer = new FileWriter(configFile);
            GSON_PRETTY.toJson(this, writer);
            writer.close();
        } catch (Exception e) {
            LOGGER.error("Failed to save config file.");
            LOGGER.error(e);
        }
    }

    public void load(File configFile) {
        // Load the config from the file
        try {
            FileReader reader = new FileReader(configFile);
            Config config = GSON_PRETTY.fromJson(reader, Config.class);
            this.connections = config.connections;
            this.savedActions = config.savedActions;
            this.savedActionKeys = config.savedActionKeys;
        } catch (Exception e) {
            LOGGER.error("Failed to load config file.");
            LOGGER.error(e);
        }
    }

    public OBSConnectionInfo getConnectionById(String obsId) {
        for (OBSConnectionInfo connection : connections) {
            if (connection.ID.equals(obsId)) {
                return connection;
            }
        }
        return null;
    }

    public ObsAction.ActionTranslatableComponent getSavedActionKey(int actionKeyId) {
        if (savedActionKeys.containsKey(actionKeyId)) {
            if (savedActions.containsKey(savedActionKeys.get(actionKeyId))) {
                return savedActions.get(savedActionKeys.get(actionKeyId));
            }
        }

        return null;
    }

    public boolean saveAction(String actionId, ObsAction action) {
        if (savedActions == null) {
            savedActions = new HashMap<>();
        }

        if (action == null) {
            LOGGER.error("Tried to save null action with ID: " + actionId);
            return false;
        }

        savedActions.put(actionId, new ObsAction.ActionTranslatableComponent(action));
        return true;
    }

    public boolean saveActionKey(int actionKeyId, String actionId) {
        if (savedActionKeys == null) {
            savedActionKeys = new HashMap<>();
        }

        if (!savedActions.containsKey(actionId)) {
            LOGGER.error("Tried to save action key with invalid action ID: " + actionId);
            return false;
        }

        savedActionKeys.put(actionKeyId, actionId);
        return true;
    }

    public static class OBSConnectionInfo {
        public String ID;
        public String host;

        public int port;

        public String password;
    }
}
