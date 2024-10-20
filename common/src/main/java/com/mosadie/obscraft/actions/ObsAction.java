package com.mosadie.obscraft.actions;

import com.mosadie.obscraft.ObsCraft;
import com.mosadie.obscraft.actions.args.Argument;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public abstract class ObsAction {

    public enum ActionType {
        SCENE,
        FILTER,
        SOURCE_VISIBILITY,
        TEXT
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
            return ObsCraft.GSON_COMPRESSED.toJson(new ActionTranslatableComponent(this));
        } catch (Exception e) {
            ObsCraft.LOGGER.error("Error creating tellraw component for action", e);
            return "";
        }
    }

    public class ActionTranslatableComponent {
        public final String type = "translatable";
        public final String translate = ObsCraft.TRANSLATION_TRIGGER;
        public final String fallback = "";

        public final String[] with;

        public ActionTranslatableComponent(ObsAction obsAction) {
            String[] withTmp = new String[] { obsAction.obsId, obsAction.type.name() };

            for (Argument arg : args) {
                withTmp = ArrayUtils.add(withTmp, ObsCraft.GSON_COMPRESSED.toJson(arg));
            }

            this.with = withTmp;
        }
    }
}
