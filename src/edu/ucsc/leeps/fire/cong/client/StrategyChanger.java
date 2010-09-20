package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;

/**
 *
 * @author jpettit
 */
public class StrategyChanger extends Thread implements Configurable<Config> {

    private final Object lock = new Object();
    private Config config;
    private volatile boolean running;
    private volatile boolean shouldUpdate;
    private float[] previousStrategy;
    private float[] currentStrategy;
    private float[] targetStrategy;
    private float[] deltaStrategy;
    private float[] lastStrategy;
    private long tickTime = 100;
    private float tickDelta;
    private long sleepTimeMillis;
    private float changeTimeEMA = 0;
    private float strategyDelta;
    private long nextAllowedChangeTime;
    private boolean initialLock;
    public volatile boolean isLocked;
    public Selector selector;

    public StrategyChanger() {
        nextAllowedChangeTime = System.currentTimeMillis();
        start();
        FIRE.client.addConfigListener(this);
        sleepTimeMillis = 50;

        //Laker
        initialLock = true;
    }

    public void configChanged(Config config) {
        synchronized (lock) {
            this.config = config;
            if (config.payoffFunction instanceof TwoStrategyPayoffFunction) {
                previousStrategy = new float[2];
                currentStrategy = new float[2];
                targetStrategy = new float[2];
                deltaStrategy = new float[2];
                lastStrategy = new float[2];
            } else if (config.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                previousStrategy = new float[3];
                currentStrategy = new float[3];
                targetStrategy = new float[3];
                deltaStrategy = new float[3];
                lastStrategy = new float[3];
            }
            recalculateTickDelta();
        }
    }

    private void update() {
        if (FIRE.client.getConfig().percentChangePerSecond >= 1.0f) {
            return;
        }
        synchronized (lock) {
            float[] temp = selector.getTarget();
            if (temp != null) {
                boolean same = true;
                for (int i = 0; i < temp.length; i++) {
                    if (Math.abs(temp[i] - currentStrategy[i]) > Float.MIN_NORMAL) {
                        same = false;
                    }
                }
                if (same) {
                    sleepTimeMillis = 100;
                    return;
                }
            }
            targetStrategy = selector.getTarget();
            float totalDelta = 0f;
            for (int i = 0; i < currentStrategy.length; i++) {
                deltaStrategy[i] = targetStrategy[i] - currentStrategy[i];
                totalDelta += Math.abs(deltaStrategy[i]);
            }
            if (totalDelta > tickDelta) {
                for (int i = 0; i < deltaStrategy.length; i++) {
                    deltaStrategy[i] = tickDelta * (deltaStrategy[i] / totalDelta);
                    currentStrategy[i] += deltaStrategy[i];
                }
            } else {
                for (int i = 0; i < currentStrategy.length; i++) {
                    currentStrategy[i] = targetStrategy[i];
                }
                sleepTimeMillis = 100;
            }

            long timestamp = System.nanoTime();
            sendUpdate();
            FIRE.client.getClient().setMyStrategy(currentStrategy);
            selector.setCurrent(currentStrategy);
            float elapsed = (System.nanoTime() - timestamp) / 1000000f;
            changeTimeEMA += 0.1 * (elapsed - changeTimeEMA);
            sleepTimeMillis = tickTime - Math.round(changeTimeEMA);
            /*
            long estimatedLag = Math.round(changeTimeEMA);
            if (tickTime > 20.0 * estimatedLag) {
            tickTime = Math.round(5.0 * estimatedLag);
            sleepTimeMillis = tickTime - Math.round(changeTimeEMA);
            recalculateTickDelta();
            } else if (sleepTimeMillis < 0) {
            tickTime = Math.round(5.0 * estimatedLag);
            sleepTimeMillis = 0;
            recalculateTickDelta();
            }
             *
             */
        }
    }

    // TODO: why does it delay AFTER the player switches strategies?
    private void sendUpdate() {
        if (config.subperiods == 0) {
            float total = 0;
            for (int i = 0; i < previousStrategy.length; i++) {
                total += Math.abs(previousStrategy[i] - currentStrategy[i]);
            }
            strategyDelta += total / 2;
        }
        FIRE.client.getServer().strategyChanged(
                currentStrategy,
                targetStrategy,
                FIRE.client.getID());
        if (config.delay != null && FIRE.client.getConfig().delay.initialLock && initialLock) {
            float delayTimeInSeconds = 0;
            switch (config.delay.distribution) {
                case uniform:
                    delayTimeInSeconds = FIRE.client.getRandom().nextFloat() * config.delay.lambda;
                    break;
                case poisson:
                    delayTimeInSeconds = generatePoisson(config.delay.lambda);
                    //delayTimeInSeconds *= 10;
                    break;
                case gaussian:
                    throw new UnsupportedOperationException();
            }
            nextAllowedChangeTime = System.currentTimeMillis() + Math.round(1000 * delayTimeInSeconds);
            initialLock = false;
            System.err.println("delaying for " + delayTimeInSeconds + " seconds");
        }
    }

    private int generatePoisson(float lambda) {
        float L = (float) Math.pow(Math.E, -1 * lambda);
        int k = 0;
        float p = 1;

        do {
            k = k + 1;
            p = p * FIRE.client.getRandom().nextFloat();
        } while (p > L);

        return k - 1;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                isLocked = decisionDelayed();
                if (selector != null) {
                    selector.setEnabled(!isLocked);
                }
                if (!isLocked && shouldUpdate) {
                    update();
                }
                sleep(sleepTimeMillis);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean decisionDelayed() {
        return System.currentTimeMillis() < nextAllowedChangeTime;
    }

    private void recalculateTickDelta() {
        tickDelta = config.percentChangePerSecond / (1000f / tickTime);
    }

    public void setCurrentStrategy(float[] strategy) {
        for (int i = 0; i < currentStrategy.length; i++) {
            previousStrategy[i] = strategy[i];
            currentStrategy[i] = strategy[i];
        }
        selector.setCurrent(currentStrategy);
    }

    public void setTargetStrategy(float[] strategy) {
        if (FIRE.client.getConfig().percentChangePerSecond >= 1.0f
                || !FIRE.client.getClient().haveInitialStrategy()) {
            for (int i = 0; i < targetStrategy.length; i++) {
                currentStrategy[i] = strategy[i];
                targetStrategy[i] = strategy[i];
            }
            FIRE.client.getClient().setMyStrategy(strategy);
            sendUpdate();
            return;
        } else {
            synchronized (lock) {
                for (int i = 0; i < targetStrategy.length; i++) {
                    targetStrategy[i] = strategy[i];
                }
            }
        }
    }

    public float getCost() {
        if (config == null) {
            return 0f;
        }
        return strategyDelta * config.changeCost;
    }

    public void startPeriod() {
        shouldUpdate = true;
        strategyDelta = 0;
    }

    public void setPause(boolean paused) {
        this.shouldUpdate = !paused;
        selector.setEnabled(!paused);
    }

    public void endSubperiod(int subperiod, float[] subperiodStrategy, float[] counterpartSubperiodStrategy) {
        if (subperiod == 1) {
            System.arraycopy(config.initialStrategy, 0, lastStrategy, 0, lastStrategy.length);
        }
        float total = 0;
        for (int i = 0; i < subperiodStrategy.length; i++) {
            total += Math.abs(subperiodStrategy[i] - lastStrategy[i]);
        }
        strategyDelta += total / 2;
        System.arraycopy(subperiodStrategy, 0, lastStrategy, 0, lastStrategy.length);
    }

    public void endPeriod() {
        shouldUpdate = false;
        selector.setEnabled(false);
    }

    public void signalStop() {
        running = false;
    }

    public float getAverageChangeTime() {
        return changeTimeEMA;
    }

    public float[] getCurrentStrategy() {
        return currentStrategy;
    }

    public static interface Selector {

        public void startPrePeriod();

        public void setEnabled(boolean enabled);

        public void setCurrent(float[] strategy);

        public void setInitial(float[] strategy);

        public void setCounterpart(float[] strategy);

        public float[] getTarget();

        public void setCurrentPercent(float percent);

        public void update();
    }
}
