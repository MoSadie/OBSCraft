package com.mosadie.obscraft.actions;

import com.mosadie.obscraft.ObsCraft;
import com.mosadie.obscraft.actions.args.Argument;
import io.obswebsocket.community.client.OBSRemoteController;

import java.util.List;

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
}
