package com.mosadie.obswscraft.actions;

import com.mosadie.obswscraft.ObsWsCraft;
import com.mosadie.obswscraft.actions.args.Argument;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public abstract class ObsAction {

    public enum ActionType {
        SCENE
    }

    public final ActionType type;

    public List<Argument> args;

    public String obsId;

    public ObsAction(ActionType type, List<Argument> args, String obsId) {
        this.type = type;
        this.args = args;
        this.obsId = obsId;
    }

    public abstract void execute();
    public String getTellRawComponent() {
        try {
            return ObsWsCraft.GSON_COMPRESSED.toJson(new ActionTranslatableComponent(obsId));
        } catch (Exception e) {
            ObsWsCraft.LOGGER.error("Error creating tellraw component for action", e);
            return "";
        }
    }

    public class ActionTranslatableComponent {
        public final String type = "translatable";
        public final String translate = ObsWsCraft.TRANSLATION_TRIGGER;
        public final String fallback = "";

        public final String[] with;

        public ActionTranslatableComponent(String obsId) {
            String[] withTmp = new String[] { obsId };

            for (Argument arg : args) {
                withTmp = ArrayUtils.add(withTmp, ObsWsCraft.GSON_COMPRESSED.toJson(arg));
            }

            this.with = withTmp;
        }
    }
}
