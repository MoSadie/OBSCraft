package com.mosadie.obswscraft;

import java.util.List;

public class Config {
    public List<OBSConnectionInfo> connections;

    public static Config defaultConfig() {
        Config config = new Config();
        OBSConnectionInfo defaultObs = new OBSConnectionInfo();
        defaultObs.ID = "default";
        defaultObs.host = "127.0.0.1";
        defaultObs.port = 4455;
        defaultObs.password = "password";
        config.connections = List.of(defaultObs);
        return config;
    }

    public static class OBSConnectionInfo {
        public String ID;
        public String host;

        public int port;

        public String password;
    }
}
