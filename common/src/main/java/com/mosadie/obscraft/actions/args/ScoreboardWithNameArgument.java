package com.mosadie.obscraft.actions.args;

import com.mosadie.obscraft.ObsCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;

/**
 * Given a scoreboard objective and a score holder, this argument will return the value of the score.
 */
public class ScoreboardWithNameArgument implements Argument{

    private final String objective;
    private final String scoreHolder;
    private final ArgumentType type = ArgumentType.SCOREBOARD_WITH_NAME;

    public ScoreboardWithNameArgument(String objective, String scoreHolder) {
        this.objective = objective;
        this.scoreHolder = scoreHolder;
    }

    @Override
    public String processArgument() {
        if (Minecraft.getInstance().level != null) {
            ReadOnlyScoreInfo scoreInfo = Minecraft.getInstance().level.getScoreboard().getPlayerScoreInfo(ScoreHolder.forNameOnly(this.scoreHolder), Minecraft.getInstance().level.getScoreboard().getObjective(this.objective));
            if (scoreInfo != null) {
                return Integer.toString(scoreInfo.value());
            } else {
                ObsCraft.LOGGER.info("Missing scoreboard value for " + this.scoreHolder + " and " + this.objective);
            }
        } else {
            ObsCraft.LOGGER.info("Scoreboard: World is null!");
        }


        return "";
    }

}
