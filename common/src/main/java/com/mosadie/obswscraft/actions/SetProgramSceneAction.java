package com.mosadie.obswscraft.actions;

import com.mosadie.obswscraft.ObsWsCraft;
import com.mosadie.obswscraft.actions.args.Argument;
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
        OBSRemoteController obs = ObsWsCraft.getOBSIfReady(obsId);
        if (obs != null) {
            String scene = args.get(0).processArgument();
            obs.setCurrentProgramScene(scene, (response) -> {
                if (response.isSuccessful()) {
                    ObsWsCraft.LOGGER.info("[OBSRawCraft] Set scene to " + scene + " on OBS " + obsId);
                } else {
                    ObsWsCraft.LOGGER.info("[OBSRawCraft] Failed to set scene to " + scene + " on OBS " + obsId);
                }
            });
        }
    }
}
