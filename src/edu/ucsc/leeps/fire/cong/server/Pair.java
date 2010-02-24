/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dev
 */
public class Pair implements Population {

    public ClientInterface player1, player2;
    public long periodStartTime;
    public long lastEvalTime;
    private float[] player1LastStrategy, player2LastStrategy;

    public void setMembers(
            List<ClientInterface> members,
            List<Population> populations,
            Map<String, Population> membership) {
        if (members.size() != 2) {
            throw new IllegalArgumentException("Pair must only be given 2 players.");
        }
        player1 = members.get(0);
        player2 = members.get(1);
        populations.add(this);
        membership.put(player1.getFullName(), this);
        membership.put(player2.getFullName(), this);
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTime = timestamp;
        player1LastStrategy = player1.getStrategy();
        player2LastStrategy = player2.getStrategy();
        player1.setOpponentStrategy(player2LastStrategy);
        player2.setOpponentStrategy(player1LastStrategy);
    }

    private float getTwoStrategyPayoffs(
            float[] mine, float[] other,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        float points = periodConfig.twoStrategyPayoffFunction.getPayoff(
                percent,
                mine[0], 1 - mine[0],
                other[0], 1 - other[0]);
        if (!periodConfig.pointsPerSecond) {
            points *= percentInStrategyTime;
        } else {
            points *= inStrategyTime / 1000f;
        }
        return points;
    }

    private float getThreeStrategyPayoffs(
            float[] mine, float[] other,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        float points = periodConfig.RPSPayoffFunction.getPayoff(
                percent,
                mine[0], mine[1], mine[2],
                other[0], other[1], other[2]);
        if (!periodConfig.pointsPerSecond) {
            points *= percentInStrategyTime;
        } else {
            points *= inStrategyTime / 1000f;
        }
        return points;
    }

    public void strategyChanged(String name, long timestamp, PeriodConfig periodConfig) {
        // update clients with payoff information for last strategy
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        long inStrategyTime = System.currentTimeMillis() - lastEvalTime;
        float percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
        if (periodConfig.twoStrategyPayoffFunction != null) {
            player1.addToPeriodPoints(getTwoStrategyPayoffs(
                    player1LastStrategy, player2LastStrategy,
                    percent, percentInStrategyTime, inStrategyTime, periodConfig));
            player2.addToPeriodPoints(getTwoStrategyPayoffs(
                    player2LastStrategy, player1LastStrategy,
                    percent, percentInStrategyTime, inStrategyTime, periodConfig));
        } else if (periodConfig.RPSPayoffFunction != null) {
            player1.addToPeriodPoints(getThreeStrategyPayoffs(
                    player1LastStrategy, player2LastStrategy,
                    percent, percentInStrategyTime, inStrategyTime, periodConfig));
            player2.addToPeriodPoints(getThreeStrategyPayoffs(
                    player2LastStrategy, player1LastStrategy,
                    percent, percentInStrategyTime, inStrategyTime, periodConfig));
        } else {
            assert false;
        }
        // update clients with new strategy information
        player1LastStrategy = player1.getStrategy();
        player2LastStrategy = player2.getStrategy();
    }
}