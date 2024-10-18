package com.mosadie.obscraft;

import com.mosadie.obscraft.actions.ObsAction;

import java.util.Map;

public class Request {
    public ObsAction.ActionType type;
    public Map<String, String> args;
}
