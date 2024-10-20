package com.mosadie.obscraft.actions;

import com.mosadie.obscraft.ObsCraft;
import com.mosadie.obscraft.actions.args.Argument;
import io.obswebsocket.community.client.OBSRemoteController;

import java.util.List;

public class SetFilterAction extends ObsAction {

    public SetFilterAction(List<Argument> args, String obsId) {
        super(ActionType.FILTER, args, obsId);
    }

    @Override
    public void execute() {
        try {
            OBSRemoteController obs = ObsCraft.getOBSIfReady(obsId);
            if (obs != null) {
                String source = args.getFirst().processArgument();
                String filter = args.get(1).processArgument();
                Visibility visiblity = Visibility.valueOf(args.get(2).processArgument().toUpperCase());

                obs.setSourceFilterEnabled(source, filter, visiblity.isVisible(), (response) -> {
                    if (response.isSuccessful()) {
                        ObsCraft.LOGGER.info("[OBSCraft] Set filter " + filter + " on " + source + " to " + (visiblity) + " on OBS " + obsId);
                    } else {
                        ObsCraft.LOGGER.info("[OBSCraft] Failed to set filter " + filter + " on " + source + " to " + (visiblity) + " on OBS " + obsId);
                    }
                });
            }
        } catch (Exception e) {
            ObsCraft.LOGGER.error("Error executing SetProgramSceneAction", e);
        }
    }
}
