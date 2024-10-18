package com.mosadie.obscraft.actions.args;

import com.mosadie.obscraft.ObsCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;

import java.util.Collection;

/**
 * Given a scoreboard objective and a score, this argument will return the name of the score holder.
 */
public class ScoreboardWithScoreArgument implements Argument{

    private final String objective;
    private final int score;
    private final ArgumentType type = ArgumentType.SCOREBOARD_WITH_SCORE;

    public ScoreboardWithScoreArgument(String objective, int score) {
        this.objective = objective;
        this.score = score;
    }

    @Override
    public String processArgument() {
        if (Minecraft.getInstance().level != null) {
            Objective objective = Minecraft.getInstance().level.getScoreboard().getObjective(this.objective);
            if (objective == null ) {
                ObsCraft.LOGGER.info("Scoreboard: Objective is null!");
                return "";
            }

            Collection<PlayerScoreEntry> scores = Minecraft.getInstance().level.getScoreboard().listPlayerScores(objective);

            for (PlayerScoreEntry scoreEntry : scores) {
                if (scoreEntry.value() == this.score) {
                    return scoreEntry.owner();
                }
            }

            ObsCraft.LOGGER.info("Missing score holder for " + this.objective + " with score " + this.score);
        } else {
            ObsCraft.LOGGER.info("Scoreboard: World is null!");
        }


        return "";
    }

}
