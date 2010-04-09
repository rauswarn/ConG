/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;

/**
 *
 * @author jpettit
 */
public class Chart extends Sprite {

    // Variables to modify that manipulate the chart
    public float currentPercent;
    private PeriodConfig periodConfig;
    private float currentPayoffYou, currentPayoffOther;
    private float maxPayoff;
    // Two strategy
    private float percent_A;
    private float percent_a;
    private float currentAPayoff;
    private float currentBPayoff;
    private float currentAaPayoff;
    private float currentAbPayoff;
    private float currentBaPayoff;
    private float currentBbPayoff;
    private Line futureAPayoff;
    private Line futureBPayoff;
    // RPSD
    private ThreeStrategySelector simplex;
    private float currentRPayoff;
    private float currentPPayoff;
    private float currentSPayoff;
    // Private controls accessed through public methods
    private Line actualPayoffYou;
    private Line actualPayoffOther;
    // Two strategy
    private Line actualAPayoff;
    private Line actualBPayoff;
    private Line actualAaPayoff;
    private Line actualAbPayoff;
    private Line actualBaPayoff;
    private Line actualBbPayoff;
    private Line futureAaPayoff;
    private Line futureAbPayoff;
    private Line futureBaPayoff;
    private Line futureBbPayoff;
    // RPSD
    private Line actualRPayoff;
    private Line actualPPayoff;
    private Line actualSPayoff;
    private Line futureRPayoff;
    private Line futurePPayoff;
    private Line futureSPayoff;
    private Line futureRrPayoff;
    private Line futureRpPayoff;
    private Line futureRsPayoff;
    private Line futurePrPayoff;
    private Line futurePpPayoff;
    private Line futurePsPayoff;
    private Line futureSrPayoff;
    private Line futureSpPayoff;
    private Line futureSsPayoff;

    public Chart(int x, int y, int width, int height, ThreeStrategySelector simplex) {
        super(x, y, width, height);
        actualPayoffYou = new Line(0, 0, width, height);
        actualPayoffOther = new Line(0, 0, width, height);
        // Two strategy
        actualAPayoff = new Line(0, 0, width, height);
        actualBPayoff = new Line(0, 0, width, height);
        futureAPayoff = new Line(0, 0, width, height);
        futureBPayoff = new Line(0, 0, width, height);
        actualAaPayoff = new Line(0, 0, width, height);
        actualAbPayoff = new Line(0, 0, width, height);
        actualBaPayoff = new Line(0, 0, width, height);
        actualBbPayoff = new Line(0, 0, width, height);
        futureAaPayoff = new Line(0, 0, width, height);
        futureAbPayoff = new Line(0, 0, width, height);
        futureBaPayoff = new Line(0, 0, width, height);
        futureBbPayoff = new Line(0, 0, width, height);
        // RPSD
        actualRPayoff = new Line(0, 0, width, height);
        actualPPayoff = new Line(0, 0, width, height);
        actualSPayoff = new Line(0, 0, width, height);

        futureRPayoff = new Line(0, 0, width, height);
        futurePPayoff = new Line(0, 0, width, height);
        futureSPayoff = new Line(0, 0, width, height);
        futureRrPayoff = new Line(0, 0, width, height);
        futureRpPayoff = new Line(0, 0, width, height);
        futureRsPayoff = new Line(0, 0, width, height);
        futurePrPayoff = new Line(0, 0, width, height);
        futurePpPayoff = new Line(0, 0, width, height);
        futurePsPayoff = new Line(0, 0, width, height);
        futureSrPayoff = new Line(0, 0, width, height);
        futureSpPayoff = new Line(0, 0, width, height);
        futureSsPayoff = new Line(0, 0, width, height);

        this.simplex = simplex;
    }

    private void drawAxis(PEmbed applet) {
        applet.textAlign(PEmbed.CENTER, PEmbed.CENTER);
        applet.fill(255);
        applet.noStroke();
        applet.rect(-40, 0, 38, height);
        applet.rect(0, height + 2, width, 40);
        for (float x = 0.1f; x < 1.0f; x += 0.1f) {
            applet.noFill();
            applet.stroke(100, 100, 100);
            applet.strokeWeight(2);
            float x0, y0, x1, y1;
            x0 = x * width;
            y0 = height;
            x1 = x * width;
            y1 = height + 10;
            applet.line(x0, y0, x1, y1);
            applet.fill(0);
            int percent = Math.round(x * 100);
            String label = String.format("%d%%", percent);
            applet.text(label, x0 + origin.x, y0 + origin.y + 1.2f * applet.textAscent() + applet.textDescent());
        }
        for (float y = 0.1f; y < 1.0f; y += 0.1f) {
            applet.noFill();
            applet.stroke(100, 100, 100);
            applet.strokeWeight(2);
            float x0, y0, x1, y1;
            x0 = -10;
            y0 = y * height;
            x1 = 0;
            y1 = y * height;
            applet.line(x0, y0, x1, y1);
            applet.fill(0);
            float payoff = (1 - y) * maxPayoff;
            String label = String.format("%.1f", payoff);
            applet.text(label, origin.x - 1.2f * applet.textWidth(label), y0 + origin.y);
        }
    }

    private void drawPercentLine(PEmbed applet) {
        applet.strokeWeight(2f);
        applet.stroke(150, 150, 150);
        applet.line(currentPercent * width, 0, currentPercent * width, height);
    }

    @Override
    public void draw(PEmbed applet) {
        applet.rectMode(PEmbed.CORNER);
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        applet.fill(255);
        applet.noStroke();
        float currX = currentPercent * width;
        applet.rect(currX - 8, 0, width, height);
        drawPercentLine(applet);
        applet.noFill();
        applet.stroke(0);
        applet.strokeWeight(2);
        applet.rect(0, 0, width, height);
        if (periodConfig != null) {
            if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
                actualAPayoff.draw(applet);
                actualBPayoff.draw(applet);
                futureAPayoff.draw(applet);
                futureBPayoff.draw(applet);
                actualAaPayoff.draw(applet);
                actualAbPayoff.draw(applet);
                actualBaPayoff.draw(applet);
                actualBbPayoff.draw(applet);
                futureAaPayoff.draw(applet);
                futureBbPayoff.draw(applet);
            } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                actualRPayoff.draw(applet);
                actualPPayoff.draw(applet);
                actualSPayoff.draw(applet);
                futureRPayoff.draw(applet);
                futurePPayoff.draw(applet);
                futureSPayoff.draw(applet);
                futureRrPayoff.draw(applet);
                futureRpPayoff.draw(applet);
                futureRsPayoff.draw(applet);
                futurePrPayoff.draw(applet);
                futurePpPayoff.draw(applet);
                futurePsPayoff.draw(applet);
                futureSrPayoff.draw(applet);
                futureSpPayoff.draw(applet);
                futureSsPayoff.draw(applet);
            }
            actualPayoffYou.draw(applet);
            actualPayoffOther.draw(applet);
        }
        drawAxis(applet);
        applet.popMatrix();
    }

    public void clearAll() {
        actualPayoffYou.clear();
        actualPayoffOther.clear();
        actualAPayoff.clear();
        actualBPayoff.clear();
        actualAaPayoff.clear();
        actualBbPayoff.clear();

        clearFuture();
    }

    public void clearFuture() {
        // clear two strategy
        futureAPayoff.clear();
        futureBPayoff.clear();
        futureAaPayoff.clear();
        futureAbPayoff.clear();
        futureBaPayoff.clear();
        futureBbPayoff.clear();

        // clear three strategy
        futureRPayoff.clear();
        futurePPayoff.clear();
        futureSPayoff.clear();
        futureRrPayoff.clear();
        futureRpPayoff.clear();
        futureRsPayoff.clear();
        futurePrPayoff.clear();
        futurePpPayoff.clear();
        futurePsPayoff.clear();
        futureSrPayoff.clear();
        futureSpPayoff.clear();
        futureSsPayoff.clear();
    }

    private void addTwoStrategyFuturePayoffPoints() {
        clearFuture();
        for (float futurePercent = currentPercent; futurePercent <= 1.0; futurePercent += 0.001f) {
            float future_A = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{1},
                    new float[]{percent_a});
            float future_B = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0},
                    new float[]{percent_a});
            float future_Aa = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{1},
                    new float[]{1});
            float future_Ab = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{1},
                    new float[]{0});
            float future_Ba = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0},
                    new float[]{1});
            float future_Bb = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0},
                    new float[]{0});
            addPoint(futureAPayoff, futurePercent, future_A);
            addPoint(futureBPayoff, futurePercent, future_B);
            addPoint(futureAaPayoff, futurePercent, future_Aa);
            addPoint(futureAbPayoff, futurePercent, future_Ab);
            addPoint(futureBaPayoff, futurePercent, future_Ba);
            addPoint(futureBbPayoff, futurePercent, future_Bb);
        }
    }

    private void addThreeStrategyFuturePayoffPoints() {
        clearFuture();
        for (float futurePercent = currentPercent; futurePercent <= 1.0; futurePercent += 0.01f) {
            float futureR = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{1, 0, 0},
                    simplex.getOpponentRPS());
            float futureP = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0, 1, 0},
                    simplex.getOpponentRPS());
            float futureS = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0, 0, 1},
                    simplex.getOpponentRPS());
            float futureRr = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{1, 0, 0},
                    new float[]{1, 0, 0});
            float futureRp = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{1, 0, 0},
                    new float[]{0, 1, 0});
            float futureRs = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{1, 0, 0},
                    new float[]{0, 0, 1});
            float futurePr = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0, 1, 0},
                    new float[]{1, 0, 0});
            float futurePp = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0, 1, 0},
                    new float[]{0, 1, 0});
            float futurePs = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0, 1, 0},
                    new float[]{0, 0, 1});
            float futureSr = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0, 0, 1},
                    new float[]{1, 0, 0});
            float futureSp = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0, 0, 1},
                    new float[]{0, 1, 0});
            float futureSs = periodConfig.payoffFunction.getPayoff(
                    futurePercent,
                    new float[]{0, 0, 1},
                    new float[]{0, 0, 1});

            addPoint(futureRPayoff, futurePercent, futureR);
            addPoint(futurePPayoff, futurePercent, futureP);
            addPoint(futureSPayoff, futurePercent, futureS);
            addPoint(futureRrPayoff, futurePercent, futureRr);
            addPoint(futureRpPayoff, futurePercent, futureRp);
            addPoint(futureRsPayoff, futurePercent, futureRs);
            addPoint(futurePrPayoff, futurePercent, futurePr);
            addPoint(futurePpPayoff, futurePercent, futurePp);
            addPoint(futurePsPayoff, futurePercent, futurePs);
            addPoint(futureSrPayoff, futurePercent, futureSr);
            addPoint(futureSpPayoff, futurePercent, futureSp);
            addPoint(futureSsPayoff, futurePercent, futureSs);
        }
    }

    public void updateLines() {
        if (currentPercent < 1.0) {
            addPoint(actualPayoffYou, currentPercent, currentPayoffYou);
            addPoint(actualPayoffOther, currentPercent, currentPayoffOther);
            if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
                addTwoStrategyActualPayoffPoints();
                addTwoStrategyFuturePayoffPoints();
            } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                addThreeStrategyActualPayoffPoints();
                addThreeStrategyFuturePayoffPoints();
            }
        }
    }

    private void addTwoStrategyActualPayoffPoints() {
        addPoint(actualAPayoff, currentPercent, currentAPayoff);
        addPoint(actualBPayoff, currentPercent, currentBPayoff);

        addPoint(actualAaPayoff, currentPercent, currentAaPayoff);
        addPoint(actualAbPayoff, currentPercent, currentAbPayoff);
        addPoint(actualBaPayoff, currentPercent, currentBaPayoff);
        addPoint(actualBbPayoff, currentPercent, currentBbPayoff);
    }

    private void addThreeStrategyActualPayoffPoints() {
        addPoint(actualRPayoff, currentPercent, currentRPayoff);
        addPoint(actualPPayoff, currentPercent, currentPPayoff);
        addPoint(actualSPayoff, currentPercent, currentSPayoff);
    }

    private void twoStrategyChanged() {
        currentPayoffYou = periodConfig.payoffFunction.getPayoff(
                currentPercent,
                new float[]{percent_A},
                new float[]{percent_a});
        currentPayoffOther = periodConfig.counterpartPayoffFunction.getPayoff(
                currentPercent,
                new float[]{percent_a},
                new float[]{percent_A});
        currentAPayoff = periodConfig.payoffFunction.getPayoff(
                currentPercent,
                new float[]{1},
                new float[]{percent_a});
        currentBPayoff = periodConfig.payoffFunction.getPayoff(
                currentPercent,
                new float[]{0},
                new float[]{percent_a});
        currentAaPayoff = periodConfig.payoffFunction.getPayoff(currentPercent,
                new float[]{1},
                new float[]{1});
        currentAbPayoff = periodConfig.payoffFunction.getPayoff(currentPercent,
                new float[]{1},
                new float[]{0});
        currentBaPayoff = periodConfig.payoffFunction.getPayoff(currentPercent,
                new float[]{0},
                new float[]{1});
        currentBbPayoff = periodConfig.payoffFunction.getPayoff(currentPercent,
                new float[]{0},
                new float[]{0});
    }

    private void threeStrategyChanged() {
        // FIXME
    }

    private void strategyChanged() {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            twoStrategyChanged();
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            threeStrategyChanged();
        } else {
            assert false;
        }
    }

    public void setMyStrategy(float[] s) {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            percent_A = s[0];
        }
        strategyChanged();
    }

    public void setOpponentStrategy(float[] s) {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            percent_a = s[0];
        }
        strategyChanged();
    }

    public void setPeriodConfig(PeriodConfig periodConfig) {
        this.periodConfig = periodConfig;
        maxPayoff = periodConfig.payoffFunction.getMax();
        actualPayoffYou.configure(periodConfig.yourPayoff);
        actualPayoffOther.configure(periodConfig.otherPayoff);
    }

    public void addPoint(Line line, float x, float y) {
        line.addPoint(
                line.width * x,
                line.height * (1 - (y / maxPayoff)));
    }
}
