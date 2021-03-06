/**
 * Copyright (c) 2012, University of California
 * All rights reserved.
 * 
 * Redistribution and use is governed by the LICENSE.txt file included with this
 * source code and available at http://leeps.ucsc.edu/cong/wiki/license
 **/
package edu.ucsc.leeps.fire.cong.config;

import edu.ucsc.leeps.fire.config.BaseConfig;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Agent;
import edu.ucsc.leeps.fire.cong.client.gui.Line;
import edu.ucsc.leeps.fire.cong.server.*;
import java.awt.Color;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author jpettit
 */
public class Config extends BaseConfig {

    public enum StrategySelector {

        heatmap2d, simplex, bubbles, strip, pure, qwerty,}

    public enum MatrixDisplayType {

        HeatmapSingle, HeatmapBoth, Corners
    }

    public enum MatchGroup {

        self, pair;
    }
    public float percentChangePerSecond;
    public PayoffFunction payoffFunction;
    public PayoffFunction counterpartPayoffFunction;
    public int numGroups;
    public int groupSize;
    public boolean assignedGroups;
    public boolean excludeSelf;
    public int subperiods;
    public boolean mixed;
    public StrategySelector selector = StrategySelector.bubbles;
    public MatrixDisplayType matrixDisplay;
    public Line yourPayoff, matchPayoff;
    public Line yourStrategy, matchStrategy;
    public Line thresholdLine;
    public String rLabel, pLabel, sLabel, shortRLabel, shortPLabel, shortSLabel;
    public Color rColor, pColor, sColor;
    public boolean showRPSSliders;
    public DecisionDelay initialDelay, delay;
    public IndefiniteEnd indefiniteEnd;
    public boolean showHeatmapLegend;
    public boolean chatroom;
    public boolean freeChat;
    public boolean negativePayoffs;
    public boolean sigmoidHeatmap;
    public float sigmoidAlpha;
    public float sigmoidBeta;
    public boolean showMatrix;
    public boolean showPayoffTimeAxisLabels;
    public boolean subperiodRematch;
    public boolean probPayoffs;
    public boolean showHeatmap;
    public boolean trajectory;
    public ChatMenu menu;
    public boolean objectiveColors;
    public boolean showSMinMax;
    public MatchGroup matchType;
    public boolean turnTaking;
    public boolean potential;
    public float grid;
    public int xAxisTicks;
    public int infoDelay;
    public String periodPointsString = "Current Points:";
    public String totalPointsString = "Previous Points:";
    public String params;
    public String agentSource;
    public String inString = "In";
    public String outString = "Out";
    public static final Class matrix2x2 = TwoStrategyPayoffFunction.class;
    public static final Class matrix3x3 = ThreeStrategyPayoffFunction.class;
    public static final Class qwerty = QWERTYPayoffFunction.class;
    public static final Class pricing = PricingPayoffFunction.class;
    public static final Class sum = SumPayoffFunction.class;
    public static final Class script = ScriptedPayoffFunction.class;
    public static final Class line = Line.class;
    public static final Class threshold = ThresholdPayoffFunction.class;
    public static final Class decisionDelay = DecisionDelay.class;
    public static final Class chatMenu = ChatMenu.class;
    public static final Class uniform = IndefiniteEnd.Uniform.class;
    public static final Class assigned = IndefiniteEnd.Assigned.class;
    // per-client
    public float[] initialStrategy;
    public float initial = Float.NaN;
    public float initial0 = Float.NaN;
    public float initial1 = Float.NaN;
    public int matchID;
    public float revealLambda = Float.NaN;
    public String revealTimes = "";
    public boolean revealAll;
    public boolean isCounterpart;
    public int playersInGroup;
    public int population, match;
    public int marginalCost;
    public int[] initiatives;
    public boolean showPGMultiplier;
    public String contributionsString;
    public int agentRefreshMillis = 100;
    public Map<String, Float> paramMap;
    public Map<String, Float[]> paramArrayMap;
    public Agent agent;

    public Config() {
        paid = true;
        length = 120;
        percentChangePerSecond = Float.NaN;
        subperiods = 0;
        preLength = 0;
        yourPayoff = new Line();
        yourPayoff.r = 50;
        yourPayoff.g = 50;
        yourPayoff.b = 50;
        yourPayoff.alpha = 100;
        yourPayoff.weight = 2f;
        yourPayoff.visible = true;
        yourPayoff.mode = Line.Mode.Shaded;
        matchPayoff = new Line();
        matchPayoff.r = 0;
        matchPayoff.g = 0;
        matchPayoff.b = 0;
        matchPayoff.alpha = 255;
        matchPayoff.weight = 2f;
        matchPayoff.visible = true;
        matchPayoff.mode = Line.Mode.Solid;
        yourStrategy = new Line();
        yourStrategy.visible = true;
        yourStrategy.mode = Line.Mode.Solid;
        yourStrategy.r = yourPayoff.r;
        yourStrategy.g = yourPayoff.g;
        yourStrategy.b = yourPayoff.b;
        yourStrategy.alpha = 100;
        yourStrategy.weight = 2f;
        matchStrategy = new Line();
        matchStrategy.visible = true;
        matchStrategy.mode = Line.Mode.Solid;
        matchStrategy.r = matchPayoff.r;
        matchStrategy.g = matchPayoff.g;
        matchStrategy.b = matchPayoff.b;
        matchStrategy.alpha = 255;
        matchStrategy.weight = 2f;
        thresholdLine = new Line();
        thresholdLine.mode = Line.Mode.Dashed;
        thresholdLine.r = 255;
        thresholdLine.g = 170;
        thresholdLine.b = 0;
        thresholdLine.alpha = 255;
        thresholdLine.weight = 2f;
        rLabel = "A";
        pLabel = "B";
        sLabel = "C";
        shortRLabel = "A";
        shortPLabel = "B";
        shortSLabel = "C";
        rColor = new Color(255, 255, 255);
        pColor = new Color(0, 0, 0);
        sColor = new Color(150, 150, 150);
        showRPSSliders = false;
        showHeatmapLegend = false;
        chatroom = false;
        negativePayoffs = false;
        sigmoidHeatmap = false;
        showHeatmap = true;
        sigmoidAlpha = 0.5f;
        showPayoffTimeAxisLabels = false;
        showMatrix = true;
        excludeSelf = false;
        groupSize = -1;
        numGroups = -1;
        population = -1;
        match = -1;
        marginalCost = 0;
        probPayoffs = false;
        trajectory = false;
        matchType = MatchGroup.self;
        menu = new ChatMenu();
        menu.m1 = "go left";
        menu.m2 = "go right";
        menu.m3 = "stay still";
        menu.m4 = "ok";
        menu.m5 = aliases[0];
        menu.m6 = aliases[1];
        menu.m7 = aliases[2];
        menu.m8 = aliases[3];
        //menu.m9 = aliases[4];
        //menu.m10 = aliases[5];
        //menu.m11 = aliases[6];
        //menu.m12 = aliases[7];
        grid = Float.NaN;
        showPGMultiplier = false;
        infoDelay = 0;
        agent = new Agent();
    }

    public float get(String key) {
        if (paramMap.containsKey(key)) {
            return paramMap.get(key);
        }
        return Float.NaN;
    }
    
    public Float[] getArray(String key) {
        if (paramArrayMap.containsKey(key)) {
            return paramArrayMap.get(key);
        }
        return null;
    }
    
    public void generateRevealedPoints(Random random) {
        if (!Float.isNaN(revealLambda)) {
            revealTimes = "\"";
            int time = 0;
            do {
                revealTimes += time + ",";
                double L = Math.exp(-revealLambda);
                int k = 0;
                double p = 1;
                do {
                    k++;
                    p = p * random.nextFloat();
                } while (p > L);
                time += k - 1;
            } while (time < length);
            revealTimes += length + "\"";
        }
    }

    public Color getColor(int id) {
        if (chatroom) {
            return currColors.get(id);
        }
        if (id == FIRE.client.getID()) {
            return colors[0];
        }
        return colors[1];
    }
    public static String[] aliases = new String[]{
        "Green", "Red", "Blue", "Orange", "Purple", "Gray", "Aqua", "Yellow"
    };
    public static Color[] colors = new Color[]{
        new Color(0x1FCB1A), // green
        new Color(0xF74018), // red
        new Color(0x587CFF), // blue
        new Color(0xFF8B00), // orange
        new Color(0xA646E0), // purple
        new Color(0xA8A8A8), // gray
        new Color(0x5DE6D7), // aqua
        new Color(0xF6FF00), // yellow
    };
    // assigned by the server in configurePeriod
    public Map<Integer, String> currAliases;
    public Map<Integer, Color> currColors;

    @Override
    public void test() throws ConfigException {
        payoffFunction.configure(this);
    }
}
