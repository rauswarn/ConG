package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.client.BaseClient;
import edu.ucsc.leeps.fire.cong.config.ClientConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;
import java.io.IOException;
import java.io.InputStream;
import processing.core.PApplet;
import processing.core.PFont;

/**
 *
 * @author jpettit
 */
public class Client extends BaseClient implements ClientInterface {

    public static final boolean DEBUG = false;
    private int width, height;
    private PEmbed embed;
    private ServerInterface server;
    private float percent;
    private PeriodConfig periodConfig;
    private ClientConfig clientConfig;
    private Countdown countdown;
    private PointsDisplay pointsDisplay;
    private TwoStrategySelector bimatrix;
    private ThreeStrategySelector simplex;
    private Chart payoffChart, strategyChart;
    private ChartLegend legend;
    private boolean isCounterpart = false;
    private StrategyChanger strategyChanger;
    public final static int QUICK_TICK_TIME = 100;

    //@Override
    public void init(edu.ucsc.leeps.fire.server.BaseServerInterface server) {
        this.server = (ServerInterface) server;
        removeAll();
        width = 900;
        height = 500;
        embed = new PEmbed(width, height);
        embed.init();
        setSize(embed.getSize());
        add(embed);
        percent = -1;
        int leftMargin = 20;
        int topMargin = 20;
        float textHeight = embed.textAscent() + embed.textDescent();
        //int matrixSize = (int) (height - (4 * textHeight) - 120);
        int matrixSize = 320;
        int counterpartMatrixSize = 100;
        strategyChanger = new StrategyChanger(this.server, this);
        bimatrix = new TwoStrategySelector(
                leftMargin, topMargin + counterpartMatrixSize + 30,
                matrixSize, counterpartMatrixSize,
                embed, strategyChanger);
        simplex = new ThreeStrategySelector(
                20, 100, 250, 600,
                embed, strategyChanger);
        countdown = new Countdown(
                counterpartMatrixSize + 4 * leftMargin, 40 + topMargin, embed);
        pointsDisplay = new PointsDisplay(
                counterpartMatrixSize + 4 * leftMargin, (int) (40 + textHeight) + topMargin, embed);
        int chartWidth = (int) (width - bimatrix.width - 2 * leftMargin - 80);
        int chartMargin = 30;
        int strategyChartHeight = 100;
        int payoffChartHeight = (int) (height - strategyChartHeight - 2 * topMargin - chartMargin - 10);
        strategyChart = new Chart(
                bimatrix.width + 80 + leftMargin, topMargin,
                chartWidth, strategyChartHeight,
                simplex, Chart.Mode.Strategy);
        payoffChart = new Chart(
                bimatrix.width + 80 + leftMargin, strategyChart.height + topMargin + chartMargin,
                chartWidth, payoffChartHeight,
                simplex, Chart.Mode.Payoff);
        legend = new ChartLegend(
                (int) (strategyChart.origin.x + strategyChart.width), (int) strategyChart.origin.y + strategyChartHeight + 3,
                0, 0);
        embed.running = true;
    }

    @Override
    public void startPeriod() {
        this.percent = 0;
        strategyChanger.startPeriod();
        simplex.setEnabled(true);
        bimatrix.setEnabled(true);
        payoffChart.clearAll();
        strategyChart.clearAll();
        super.startPeriod();
    }

    @Override
    public void endPeriod() {
        strategyChanger.endPeriod();
        simplex.reset();
        bimatrix.setEnabled(false);
        super.endPeriod();
    }

    @Override
    public void setPause(boolean paused) {
        strategyChanger.setPause(paused);
        if (paused) {
            simplex.setEnabled(true);
        } else {
            simplex.pause();
        }
        bimatrix.setEnabled(!paused);
        super.setPause(paused);
    }

    @Override
    public void setPeriodConfig(BasePeriodConfig basePeriodConfig) {
        super.setPeriodConfig(basePeriodConfig);
        periodConfig = (PeriodConfig) basePeriodConfig;
        if (isCounterpart) {
            PayoffFunction tmp = periodConfig.payoffFunction;
            periodConfig.payoffFunction = periodConfig.counterpartPayoffFunction;
            periodConfig.counterpartPayoffFunction = tmp;
        }
        //this.clientConfig = (ClientConfig) superPeriodConfig.clientConfigs.get(getID());
        strategyChanger.setPeriodConfig(periodConfig);
        bimatrix.setPeriodConfig(periodConfig);
        simplex.setPeriodConfig(periodConfig);
        payoffChart.setPeriodConfig(periodConfig);
        strategyChart.setPeriodConfig(periodConfig);
        legend.setPeriodConfig(periodConfig);
    }

    @Override
    public void setPeriodPoints(float periodPoints) {
        super.setPeriodPoints(periodPoints);
        pointsDisplay.setPoints(periodPoints, totalPoints);
    }

    @Override
    public void addToPeriodPoints(float points) {
        super.addToPeriodPoints(points);
        pointsDisplay.setPoints(periodPoints, totalPoints);
    }

    public void localTick(int secondsLeft) {
        this.percent = embed.width * (1 - (secondsLeft / (float) periodConfig.length));
        countdown.setSecondsLeft(secondsLeft);
        bimatrix.update();
        simplex.update();
    }

    public void quickTick(int millisLeft) {
        if (millisLeft > 0) {
            this.percent = (1 - (millisLeft / ((float) periodConfig.length * 1000)));
            payoffChart.currentPercent = this.percent;
            payoffChart.updateLines();
            strategyChart.currentPercent = this.percent;
            strategyChart.updateLines();
            bimatrix.setCurrentPercent(this.percent);
            simplex.currentPercent = this.percent;
        }
    }

    public void setActionsEnabled(boolean enabled) {
        simplex.setEnabled(enabled);
    }

    public synchronized float[] getStrategy() {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            return bimatrix.getMyStrategy();
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            return simplex.getPlayerRPS();
        } else {
            assert false;
            return new float[]{};
        }
    }

    public synchronized void setMyStrategy(float[] s) {
        strategyChanger.setCurrentStrategy(s);
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setMyStrategy(s[0]);
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setPlayerRPS(s[0], s[1], s[2]);
        } else {
            assert false;
        }
        payoffChart.setMyStrategy(s);
        strategyChart.setMyStrategy(s);
    }

    public synchronized void setCounterpartStrategy(float[] s) {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setCounterpartStrategy(s[0]);
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setCounterpartRPS(s[0], s[1], s[2]);
        } else {
            assert false;
        }
        payoffChart.setCounterpartStrategy(s);
        strategyChart.setCounterpartStrategy(s);
    }

    @Override
    public int getQuickTickInterval() {
        return QUICK_TICK_TIME;
    }

    public void setIsCounterpart(boolean isCounterpart) {
        this.isCounterpart = isCounterpart;
        bimatrix.setIsCounterpart(isCounterpart);
        payoffChart.setIsCounterpart(isCounterpart);
    }

    public void setTwoStrategyHeatmapBuffers(float[][][] payoff, float[][][] counterpartPayoff) {
        if (isCounterpart) {
            bimatrix.setTwoStrategyHeatmapBuffers(counterpartPayoff, payoff);
        } else {
            bimatrix.setTwoStrategyHeatmapBuffers(payoff, counterpartPayoff);
        }
    }

    public class PEmbed extends PApplet {

        private final String RENDERER = P2D;
        private int initWidth, initHeight;
        public PFont size14, size14Bold, size16, size16Bold, size18, size18Bold, size24, size24Bold;
        public boolean running = false;

        public PEmbed(int initWidth, int initHeight) {
            this.initWidth = initWidth;
            this.initHeight = initHeight;
            try {
                InputStream fontInputStream;
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-14.vlw");
                size14 = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-Bold-14.vlw");
                size14Bold = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-16.vlw");
                size16 = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-Bold-16.vlw");
                size16Bold = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-18.vlw");
                size18 = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-Bold-18.vlw");
                size18Bold = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-24.vlw");
                size24 = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-Bold-24.vlw");
                size24Bold = new PFont(fontInputStream);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

        @Override
        public void setup() {
            size(initWidth, initHeight, RENDERER);
            smooth();
            textFont(size14);
            textMode(SCREEN);
        }

        @Override
        public void draw() {
            if (running) {
                background(255);
                bimatrix.draw(embed);
                simplex.draw(embed);
                strategyChart.draw(embed);
                payoffChart.draw(embed);
                legend.draw(embed);
                countdown.draw(embed);
                pointsDisplay.draw(embed);
                if (DEBUG) {
                    String frameRateString = String.format("FPS: %.2f", frameRate);
                    if (frameRate < 8) {
                        fill(255, 0, 0);
                    } else {
                        fill(0);
                    }
                    text(frameRateString, 330, 30);
                    float averageChangeTime = strategyChanger.getAverageChangeTime();
                    String changeTimeString = String.format("MPC: %.2f", averageChangeTime);
                    if (averageChangeTime > 10) {
                        fill(255, 0, 0);
                    } else {
                        fill(0);
                    }
                    text(changeTimeString, 330, 45);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        if (args.length == 3) {
            Client.start(
                    args[0], args[1], args[2],
                    client, ServerInterface.class, ClientInterface.class);
        } else {
            Client.start(client, ServerInterface.class, ClientInterface.class);
        }
    }
}
