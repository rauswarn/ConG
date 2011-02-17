package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.FIREClientInterface;
import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.gui.TwoStrategySelector;
import edu.ucsc.leeps.fire.cong.client.gui.Countdown;
import edu.ucsc.leeps.fire.cong.client.gui.ChartLegend;
import edu.ucsc.leeps.fire.cong.client.gui.PointsDisplay;
import edu.ucsc.leeps.fire.cong.client.gui.ThreeStrategySelector;
import edu.ucsc.leeps.fire.cong.client.gui.Chart;
import edu.ucsc.leeps.fire.cong.client.gui.PureStrategySelector;
import edu.ucsc.leeps.fire.cong.client.gui.OneStrategyStripSelector;
import edu.ucsc.leeps.fire.cong.client.gui.Chatroom;
import edu.ucsc.leeps.fire.cong.client.gui.CournotSelector;
import edu.ucsc.leeps.fire.cong.client.gui.QWERTYStrategySelector;
import edu.ucsc.leeps.fire.cong.client.gui.Sprite;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.CournotPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.PricingPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import javax.media.opengl.GLException;
import javax.swing.JFrame;
import processing.core.PApplet;
import processing.core.PFont;

/**
 *
 * @author jpettit
 */
public class Client extends PApplet implements ClientInterface, FIREClientInterface, Configurable<Config> {

    public static boolean ALLOW_DEBUG = System.getProperty("fire.client.debug") != null;
    public final static boolean USE_OPENGL = true;
    public static State state;
    public boolean debug;
    public int framesPerUpdate;
    private Countdown countdown;
    private PointsDisplay pointsDisplay;
    //only one shown
    private TwoStrategySelector bimatrix;
    private ThreeStrategySelector simplex;
    private PureStrategySelector pureMatrix;
    private OneStrategyStripSelector strip;
    private QWERTYStrategySelector qwerty;
    private CournotSelector cournot;
    private Sprite selector;
    private Chart payoffChart, strategyChart;
    private Chart rChart, pChart, sChart;
    // heatmap legend off/on
    private ChartLegend legend;
    private StrategyChanger strategyChanger;
    private JFrame chatFrame;
    private Chatroom chatroom;
    private boolean haveInitialStrategy;
    private int INIT_WIDTH, INIT_HEIGHT;
    public PFont size14, size14Bold, size16, size16Bold, size18, size18Bold, size24, size24Bold;
    private Agent agent;
    private boolean fullscreen = false;
    private boolean resize = false;
    private WindowAdapter windowListener = new WindowAdapter() {

        @Override
        public void windowIconified(WindowEvent e) {
            resize = true;
        }
    };

    public Client() {
        FIRE.client.addConfigListener(this);
        loadLibraries();
        noLoop();
        INIT_WIDTH = 900;
        INIT_HEIGHT = 600;
        frame = new JFrame();
        frame.setTitle("CONG - " + FIRE.client.getName());
        ((JFrame) frame).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(this);
        frame.setSize(INIT_WIDTH, INIT_HEIGHT);
        frame.setResizable(false);
        frame.addWindowListener(windowListener);
        init();
        noLoop();
        frame.setVisible(true);
        loop();
        debug = ALLOW_DEBUG;
        agent = new Agent(debug);
        agent.start();
        chatFrame = new JFrame("Chat");
        chatroom = new Chatroom(chatFrame);
        chatFrame.add(chatroom);
        chatFrame.setVisible(false);
    }

    public boolean haveInitialStrategy() {
        return haveInitialStrategy;
    }

    public void startPrePeriod() {
        state.id = FIRE.client.getID();
        if (FIRE.client.getConfig().initialStrategy == null) {
            haveInitialStrategy = false;
        } else {
            haveInitialStrategy = true;
        }
        state.currentPercent = 0;
        if (simplex.visible) {
            selector = simplex;
            strategyChanger.selector = simplex;
        } else if (bimatrix.visible) {
            selector = bimatrix;
            strategyChanger.selector = bimatrix;
        } else if (pureMatrix.visible) {
            selector = pureMatrix;
            strategyChanger.selector = pureMatrix;
        } else if (strip.visible) {
            selector = strip;
            strategyChanger.selector = strip;
        } else if (qwerty.visible) {
            selector = qwerty;
            strategyChanger.selector = qwerty;
        } else if (cournot.visible) {
            selector = cournot;
            strategyChanger.selector = cournot;
        }
        strategyChanger.selector.startPrePeriod();
        payoffChart.clearAll();
        strategyChart.clearAll();
        rChart.clearAll();
        pChart.clearAll();
        sChart.clearAll();
    }

    public void startPeriod() {
        state.subperiod = 0;
        state.setMyStrategy(FIRE.client.getConfig().initialStrategy);
        if (FIRE.client.getConfig().preLength == 0) {
            state.currentPercent = 0;
            bimatrix.setEnabled(true);
            pureMatrix.setEnabled(true);
            strip.setEnabled(true);
            payoffChart.clearAll();
            strategyChart.clearAll();
            rChart.clearAll();
            pChart.clearAll();
            sChart.clearAll();
        }
        simplex.setEnabled(true);

        strategyChanger.startPeriod();
        strategyChanger.selector.startPeriod();

        state.currentPercent = 0f;
        if (FIRE.client.getConfig().subperiods == 0) {
            payoffChart.updateLines();
            strategyChart.updateLines();
            rChart.updateLines();
            pChart.updateLines();
            sChart.updateLines();
        } else {
            payoffChart.endSubperiod(0);
            strategyChart.endSubperiod(0);
            rChart.endSubperiod(0);
            pChart.endSubperiod(0);
            sChart.endSubperiod(0);
        }
        pointsDisplay.startPeriod();
    }

    public void endPeriod() {
        strategyChanger.endPeriod();

        state.currentPercent = 1f;

        if (FIRE.client.getConfig().subperiods == 0) {
            payoffChart.updateLines();
            strategyChart.updateLines();
            rChart.updateLines();
            pChart.updateLines();
            sChart.updateLines();
        }
        pointsDisplay.endPeriod();

    }

    public float getCost() {
        return strategyChanger.getCost();
    }

    public void setIsPaused(boolean isPaused) {
        strategyChanger.setPause(isPaused);
    }

    public void tick(int secondsLeft) {
        state.currentPercent = (1 - (secondsLeft / (float) FIRE.client.getConfig().length));
        countdown.setSecondsLeft(secondsLeft);
    }

    public void setStrategies(Map<Integer, float[]> strategies) {
        state.strategies = strategies;
    }

    public void setMatchStrategies(Map<Integer, float[]> matchStrategies) {
        state.matchStrategies = matchStrategies;
    }

    public void endSubperiod(
            final int subperiod,
            final Map<Integer, float[]> strategies,
            final Map<Integer, float[]> matchStrategies,
            final float payoff, final float matchPayoff) {
        new Thread() {

            @Override
            public void run() {
                state.subperiod = subperiod;
                state.strategies = strategies;
                state.matchStrategies = matchStrategies;
                state.subperiodPayoff = payoff;
                state.subperiodMatchPayoff = matchPayoff;
                payoffChart.endSubperiod(subperiod);
                strategyChart.endSubperiod(subperiod);
                rChart.endSubperiod(subperiod);
                pChart.endSubperiod(subperiod);
                sChart.endSubperiod(subperiod);
                strategyChanger.endSubperiod(subperiod);
            }
        }.start();
    }

    public void newMessage(String message) {
        chatroom.newMessage(message);
    }

    public boolean readyForNextPeriod() {
        return true;
    }

    /**
     * Terminates the virtual machine upon disconnect.
     */
    public void disconnect() {
        System.exit(0);
    }

    @Override
    public void setup() {
        textMode(MODEL);
        if (USE_OPENGL) {
            try {
                size(INIT_WIDTH, INIT_HEIGHT - 40, OPENGL);
            } catch (GLException ex1) {
                try {
                    size(INIT_WIDTH, INIT_HEIGHT - 40, OPENGL);
                } catch (GLException ex2) {
                }
            }
            hint(DISABLE_OPENGL_2X_SMOOTH);
            hint(DISABLE_OPENGL_ERROR_REPORT);
            hint(DISABLE_DEPTH_TEST);
        } else {
            size(INIT_WIDTH, INIT_HEIGHT - 40, P2D);
            frameRate(35);
        }
        setupFonts();
        textFont(size14);
        width = INIT_WIDTH;
        height = INIT_HEIGHT - 40;

        int leftMargin = 20;
        int topMargin = 20;
        float textHeight = textAscent() + textDescent();
        int matrixSize = (int) (height - (4 * textHeight) - 120);
        int counterpartMatrixSize = 100;
        strategyChanger = new StrategyChanger();
        Client.state = new State(strategyChanger);
        bimatrix = new TwoStrategySelector(
                null, leftMargin, topMargin + counterpartMatrixSize + 30,
                matrixSize, counterpartMatrixSize,
                this);
        simplex = new ThreeStrategySelector(
                null, 60, 250, 300, 600,
                this);
        pureMatrix = new PureStrategySelector(
                null, leftMargin, topMargin + counterpartMatrixSize + 30,
                matrixSize, this);
        strip = new OneStrategyStripSelector(null,
                leftMargin + 7 * matrixSize / 8, topMargin + counterpartMatrixSize + 30,
                matrixSize / 8, matrixSize,
                this);
        qwerty = new QWERTYStrategySelector(
                null, leftMargin, topMargin + counterpartMatrixSize + 30,
                matrixSize,
                this);
        cournot = new CournotSelector(null, leftMargin, topMargin + counterpartMatrixSize,
                matrixSize, matrixSize, this);
        countdown = new Countdown(
                null, bimatrix.width - 150, 20 + topMargin, this);
        pointsDisplay = new PointsDisplay(
                null, bimatrix.width - 150, (int) (20 + textHeight) + topMargin, this);
        int chartLeftOffset = bimatrix.width;
        int chartWidth = (int) (width - chartLeftOffset - 2 * leftMargin - 80);
        int chartMargin = 30;
        int strategyChartHeight = 100;
        int threeStrategyChartHeight = 30;
        int payoffChartHeight = (int) (height - strategyChartHeight - 2 * topMargin - chartMargin - 10);
        strategyChart = new Chart(
                null, chartLeftOffset + 80 + leftMargin, topMargin,
                chartWidth, strategyChartHeight,
                simplex, Chart.Mode.TwoStrategy);
        payoffChart = new Chart(
                null, chartLeftOffset + 80 + leftMargin, strategyChart.height + topMargin + chartMargin,
                chartWidth, payoffChartHeight,
                simplex, Chart.Mode.Payoff);
        rChart = new Chart(
                null, chartLeftOffset + 80 + leftMargin, topMargin,
                chartWidth, threeStrategyChartHeight,
                simplex, Chart.Mode.RStrategy);
        pChart = new Chart(
                null, chartLeftOffset + 80 + leftMargin, topMargin + threeStrategyChartHeight + 5,
                chartWidth, threeStrategyChartHeight,
                simplex, Chart.Mode.PStrategy);
        sChart = new Chart(
                null, chartLeftOffset + 80 + leftMargin, topMargin + 2 * (threeStrategyChartHeight + 5),
                chartWidth, threeStrategyChartHeight,
                simplex, Chart.Mode.SStrategy);
        legend = new ChartLegend(
                null, (int) (strategyChart.origin.x + strategyChart.width), (int) strategyChart.origin.y + strategyChartHeight + 3,
                0, 0);
    }

    @Override
    public void draw() {
        try {
            if (resize) {
                setupFonts();
                textFont(size14);
                resize = false;
            }

            background(255);
            if (FIRE.client.getConfig() == null) {
                fill(0);
                String s = "Please wait for the experiment to begin";
                text(s, Math.round(width / 2 - textWidth(s) / 2), Math.round(height / 2));
                return;
            }

            if (FIRE.client.isRunningPeriod() && !FIRE.client.isPaused()) {
                long length = FIRE.client.getConfig().length * 1000l;
                state.currentPercent = (float) FIRE.client.getElapsedMillis() / (float) length;

                if (FIRE.client.getConfig().subperiods == 0) {
                    payoffChart.updateLines();
                    strategyChart.updateLines();
                    rChart.updateLines();
                    pChart.updateLines();
                    sChart.updateLines();
                }
            }
            if (FIRE.client.isRunningPeriod()) {
                pointsDisplay.update();
            }

            if (selector != null) {
                selector.draw(this);
            }
            if (FIRE.client.getConfig() != null) {
                payoffChart.draw(this);
                strategyChart.draw(this);
                rChart.draw(this);
                pChart.draw(this);
                sChart.draw(this);
            }
            legend.draw(this);
            if (FIRE.client.getConfig().preLength > 0 && !haveInitialStrategy) {
                float textHeight = textDescent() + textAscent() + 8;
                fill(255, 50, 50);
                text("Please choose an initial strategy.", Math.round(countdown.origin.x), Math.round(countdown.origin.y - textHeight));
            }
            countdown.draw(this);
            pointsDisplay.draw(this);
            if (debug) {
                String frameRateString = String.format("FPS: %.2f, Agent: %s", frameRate, agent.paused ? "off" : "on");
                if (frameRate < 8) {
                    fill(255, 0, 0);
                } else {
                    fill(0);
                }
                text(frameRateString, 10, height - 10);
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void configChanged(Config config) {
        framesPerUpdate = Math.round(frameRateTarget * (1f / FIRE.client.getConfig().updatesPerSecond));
        payoffChart.setVisible(true);
        legend.setVisible(true);
        strategyChart.setVisible(false);
        rChart.setVisible(false);
        pChart.setVisible(false);
        sChart.setVisible(false);
        if (config.payoffFunction instanceof TwoStrategyPayoffFunction) {
            if (config.payoffFunction instanceof PricingPayoffFunction) {
                payoffChart.width = width - 110;
                payoffChart.origin.x = 100;
                payoffChart.clearAll();
                strip.origin.x = 10;
                legend.setVisible(false);
                payoffChart.configChanged(config);
            } else if (config.payoffFunction instanceof CournotPayoffFunction) {
                strategyChart.setVisible(false);
                payoffChart.setVisible(false);
                legend.setVisible(false);
                cournot.width = Math.round(0.7f * width);
                cournot.origin.x = 0.15f * width;
            } else {
                strategyChart.setVisible(true);
            }
        } else {
            rChart.setVisible(true);
            pChart.setVisible(true);
            sChart.setVisible(true);
        }
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (ALLOW_DEBUG && ke.getKeyCode() == KeyEvent.VK_D) {
            debug = !debug;
        } else if (ALLOW_DEBUG && ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_A) {
            agent.paused = !agent.paused;
        } else if (ke.isAltDown() && ke.getKeyCode() == KeyEvent.VK_F11) {
            fullscreen = !fullscreen;
            if (fullscreen) {
                frame.removeAll();
                frame.dispose();
                frame = new JFrame();
                frame.setTitle("CONG - " + FIRE.client.getName());
                ((JFrame) frame).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.add(this);
                frame.setResizable(false);
                frame.setUndecorated(true);
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
                frame.setVisible(true);
                frame.addWindowListener(windowListener);
            } else {
                frame.removeAll();
                frame.dispose();
                frame = new JFrame();
                frame.setTitle("CONG - " + FIRE.client.getName());
                ((JFrame) frame).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.add(this);
                frame.setSize(INIT_WIDTH, INIT_HEIGHT);
                frame.setResizable(false);
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
                frame.setVisible(true);
                frame.addWindowListener(windowListener);
            }
            resize = true;
        } else if (ke.isAltDown() && ke.getKeyCode() == KeyEvent.VK_R) {
            resize = true;
        }
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        chatroom.addCharacter(ke.getKeyChar());
    }

    private void setupFonts() {
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

    private void loadLibraries() {
        System.err.println("os.arch: " + System.getProperty("os.arch"));
        if (System.getProperty("os.arch").equals("amd64")) {
            addDir("./lib/64-bit");
        } else {
            addDir("./lib/32-bit");
            String tmpDir = System.getProperty("java.io.tmpdir");
            List<JarEntry> entries = new LinkedList<JarEntry>();
            for (String pathItem : System.getProperty("java.class.path").split(":")) {
                if (pathItem.contains("jar")) {
                    try {
                        JarFile jar = new JarFile(pathItem);
                        JarInputStream jarInputStream = new JarInputStream(new FileInputStream(pathItem));
                        JarEntry entry = jarInputStream.getNextJarEntry();
                        while (entry != null) {
                            if (entry.getName().endsWith("so")
                                    || entry.getName().endsWith("dll")
                                    || entry.getName().endsWith("jnilib")
                                    || entry.getName().endsWith("dynlib")) {
                                entries.add(entry);
                            }
                            entry = jarInputStream.getNextJarEntry();
                        }
                        for (JarEntry toExtract : entries) {
                            File tmpFile = new File(tmpDir, toExtract.getName());
                            OutputStream out = new FileOutputStream(tmpFile);
                            InputStream in = jar.getInputStream(toExtract);
                            byte[] buffer = new byte[4096];
                            while (true) {
                                int nBytes = in.read(buffer);
                                if (nBytes <= 0) {
                                    break;
                                }
                                out.write(buffer, 0, nBytes);
                            }
                            out.flush();
                            out.close();
                            in.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
            }
            addDir(tmpDir);
        }
    }

    public static void addDir(String s) {
        System.err.println("adding " + s + " to class path");
        try {
            // This enables the java.library.path to be modified at runtime
            // From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
            //
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (int i = 0; i < paths.length; i++) {
                if (s.equals(paths[i])) {
                    return;
                }
            }
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = s;
            field.set(null, tmp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FIRE.startClient();
    }
}
