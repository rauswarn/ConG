
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.client.gui.Slider;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.PayoffUtils;
import edu.ucsc.leeps.fire.cong.server.ScriptedPayoffFunction.PayoffScriptInterface;
import edu.ucsc.leeps.fire.cong.server.SumPayoffFunction;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Hotelling implements PayoffScriptInterface, MouseListener, KeyListener {

    private boolean enabled = true;
    private Slider slider;
    private Config config;
    private float[] subperiodStrategy;
    private boolean setup = false;
    private float width, height;

    public Hotelling() {
        if (FIRE.client != null) {
            config = FIRE.client.getConfig();
        } else if (FIRE.server != null) {
            config = FIRE.server.getConfig();
        }
    }

    public float getPayoff(
            int id,
            float percent,
            Map<Integer, float[]> popStrategies,
            Map<Integer, float[]> matchPopStrategies,
            Config config) {
        if (popStrategies.size() < 2) { // if only 1 person is playing, they get zero
            return 0;
        }

        SortedSet<Float> sorted = new TreeSet<Float>();
        for (float[] s : popStrategies.values()) {
            sorted.add(s[0]);
        }

        float s = popStrategies.get(id)[0];
        SortedSet<Float> leftSide = sorted.headSet(s);
        SortedSet<Float> rightSide = sorted.tailSet(s);
        rightSide.remove(s); // remove s from right side because tailSet is inclusive
        float left, right;
        if (leftSide.isEmpty()) {
            left = 0;
        } else {
            left = leftSide.last();
        }
        if (rightSide.isEmpty()) {
            right = 1f;
        } else {
            right = rightSide.first();
        }
        float u;
        if (left == 0) {
            u = s + 0.5f * (right - s);
        } else if (right == 1f) {
            u = 0.5f * (s - left) + (1 - s);
        } else {
            u = 0.5f * (s - left) + 0.5f * (right - s);
        }

        int shared = 0; // shared must be at least 1 after the loop, as you have to share your own strategy
        for (int otherId : popStrategies.keySet()) {
            if (popStrategies.get(otherId)[0] == s) {
                shared++;
            }
        }
        assert shared >= 1;
        return config.get("Alpha") * 100 * (u / shared);
    }

    public void draw(Client a) {
        
        width = 0.8f * a.width;
        height = 0.8f * a.height;

        if (!setup && Client.state != null && Client.state.getMyStrategy() != null) {
            slider = new Slider(a, Slider.Alignment.Horizontal,
                    0, a.width, a.height, Color.black, "", 1f);
            slider.setShowStrategyLabel(false);
            slider.hideGhost();
            slider.setOutline(true);
            a.addMouseListener(this);
            a.addKeyListener(this);
            subperiodStrategy = new float[1];
            slider.setStratValue(Client.state.getMyStrategy()[0]);
            slider.setGhostValue(slider.getStratValue());
            setup = true;
        }

        slider.sliderStart = 0;
        slider.sliderEnd = a.width;
        slider.length = a.width;

        if (Client.state.getMyStrategy() != null) {
            slider.setStratValue(Client.state.getMyStrategy()[0]);
        }
        if (Client.state.target != null) {
            slider.setGhostValue(Client.state.target[0]);
        }
        if (config.subperiods != 0 && Client.state.target != null) {
            slider.setStratValue(Client.state.target[0]);
        }

        if (enabled && !config.trajectory && slider.isGhostGrabbed()) {
            float mouseX = a.mouseX;
            slider.moveGhost(mouseX);
            setTarget(slider.getGhostValue());
        }

        a.pushMatrix();
        try {
            
            a.translate(0, 0);
            
            if (config.potential) {
                drawPotentialPayoffs(a);
            }

            if (config.payoffFunction.getNumStrategies() == 2) {
                drawInOutButtons(a);
            }

            drawAxis(a);

            slider.draw(a);

            int i = 1;
            for (int id : Client.state.strategies.keySet()) {
                Color color;
                if (config.objectiveColors) {
                    color = config.currColors.get(id);
                } else {
                    if (id == FIRE.client.getID()) {
                        color = Config.colors[0];
                    } else {
                        color = Config.colors[i];
                        i++;
                    }
                }
                drawStrategy(a, color, id);
            }
            if (config.subperiods != 0 && FIRE.client.isRunningPeriod()) {
                drawPlannedStrategy(a);
            }
            if (config.objectiveColors) {
                a.fill(config.currColors.get(FIRE.client.getID()).getRGB());
                a.text(config.currAliases.get(FIRE.client.getID()), 0, -10);
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        a.popMatrix();
    }

    public float getMax() {
        return 100;
    }

    public float getMin() {
        return 0;
    }

    private void drawPotentialPayoffs(Client a) {
        float[] s = {0};
        float max = config.payoffFunction.getMax();
        a.stroke(50);
        for (float x = 0; x < a.width; x++) {
            s[0] = x / a.width;
            float u = PayoffUtils.getPayoff(s);
            float y = u / max;
            a.point(x, a.height * (1 - y));
        }
    }

    private void drawInOutButtons(Client a) {
        a.stroke(0, 0, 0, 255);
        a.rectMode(Client.CORNERS);
        a.strokeWeight(3);
        a.fill(255, 255, 255, 255);
        a.rect(a.width - 100, -5, a.width, -30);
        a.rect(a.width - 205, -5, a.width - 105, -30);
        a.fill(0, 0, 0, 100);
        if (Client.state.getMyStrategy() != null) {
            if (Client.state.getMyStrategy()[1] == 0) {
                a.rect(a.width - 100, -5, a.width, -30);
            } else {
                a.rect(a.width - 205, -5, a.width - 105, -30);
            }
        }
        a.fill(0, 0, 0, 255);
        a.textAlign(Client.CENTER, Client.CENTER);
        float in_w = a.textWidth(config.inString);
        float out_w = a.textWidth(config.outString);
        float h = a.textAscent() + a.textDescent();
        a.text(config.outString, a.width - 50 - out_w / 2, -25 + h / 2);
        a.text(config.inString, a.width - 150 - in_w / 2, -25 + h / 2);
    }

    private void drawPlannedStrategy(Client a) {
        a.stroke(0, 0, 0, 20);
        a.line(a.width * Client.state.target[0], 0, a.width * Client.state.target[0], a.height);
    }

    private void drawStrategy(Client applet, Color color, int id) {
        if (Client.state.strategiesTime.size() < 1) {
            return;
        }
        float x, y, min, max;
        min = config.payoffFunction.getMin();
        max = config.payoffFunction.getMax();
        float payoff;
        float[] strategy;
        if (config.subperiods != 0) {
            payoff = config.payoffFunction.getPayoff(
                    id, 0, Client.state.getFictitiousStrategies(FIRE.client.getID(), subperiodStrategy), null, config);
            if (!Client.state.strategiesTime.isEmpty()) {
                strategy = Client.state.strategiesTime.get(Client.state.strategiesTime.size() - 1).strategies.get(id);
            } else {
                strategy = config.initialStrategy;
            }
        } else {
            strategy = Client.state.strategies.get(id);
            payoff = PayoffUtils.getPayoff(id, strategy);
        }
        x = applet.width * strategy[0];
        y = applet.height * (1 - (payoff - min) / (max - min));
        if (y > applet.height) {
            y = applet.height;
        } else if (y < 0) {
            y = 0;
        }
        applet.stroke(color.getRed(), color.getGreen(), color.getBlue());
        applet.fill(color.getRed(), color.getGreen(), color.getBlue());
        if (id != FIRE.client.getID() && config.subperiods == 0 || Client.state.subperiod != 0) {
            applet.strokeWeight(3);
            applet.line(x, applet.height - 5, x, applet.height + 5);
        }
        if (config.subperiods == 0 || Client.state.subperiod != 0) {
            applet.strokeWeight(1);
            if (id == FIRE.client.getID()) {
                applet.ellipse(x, y, 11, 11);
            } else {
                applet.ellipse(x, y, 8, 8);
            }
        }
        if (config.subperiods == 0 || Client.state.subperiod != 0) {
            applet.textAlign(Client.RIGHT, Client.CENTER);
            String label = String.format("%.1f", payoff);
            applet.text(label, Math.round(x - 5), Math.round(y - 6));
        }
        if (payoff > max && (config.subperiods == 0 || Client.state.subperiod != 0)) {
            drawUpArrow(applet, color, x);
        } else if (payoff < min && (config.subperiods == 0 || Client.state.subperiod != 0)) {
            drawDownArrow(applet, color, x);
        }
    }

    private void drawUpArrow(Client applet, Color color, float x) {
        applet.strokeWeight(3f);
        applet.line(x, -22, x, -10);
        applet.noStroke();
        applet.triangle(x - 5, -20, x, -30, x + 5, -20);
    }

    private void drawDownArrow(Client applet, Color color, float x) {
        applet.strokeWeight(3f);
        applet.line(x, applet.height + 10, x, applet.height + 22);
        applet.noStroke();
        applet.triangle(x - 5, applet.height + 20, x, applet.height + 30, x + 5, applet.height + 20);
    }

    private void drawAxis(Client applet) {
        float min, max;
        min = config.payoffFunction.getMin();
        max = config.payoffFunction.getMax();
        applet.rectMode(Client.CORNER);
        applet.noFill();
        applet.stroke(0);
        applet.strokeWeight(2);
        applet.rect(0, 0, applet.width, applet.height);

        applet.textAlign(Client.CENTER, Client.CENTER);
        applet.fill(255);
        applet.noStroke();
        applet.rect(-40, 0, 38, applet.height);
        applet.rect(0, applet.height + 2, applet.width, 40);
        String maxPayoffLabel = String.format("%.1f", max);
        float labelX = 10 + applet.width + 1.1f * applet.textWidth(maxPayoffLabel) / 2f;
        for (float y = 0.0f; y <= 1.01f; y += 0.1f) {
            applet.noFill();
            applet.stroke(100, 100, 100);
            applet.strokeWeight(2);
            float x0, y0, x1, y1;
            x0 = 0;
            y0 = y * applet.height;
            x1 = applet.width + 10;
            y1 = y * applet.height;
            applet.stroke(100, 100, 100, 50);
            applet.line(x0, y0, x1, y1);
            float payoff = (1 - y) * (max - min) + min;
            if (payoff < 0) {
                payoff = 0f;
            }
            applet.fill(0);
            String label = String.format("%.1f", payoff);
            applet.text(label, Math.round(labelX), Math.round(y0));
        }
        if (config.payoffFunction instanceof SumPayoffFunction && config.showSMinMax) { //payoff function dependent
            SumPayoffFunction pf = (SumPayoffFunction) config.payoffFunction;
            String label = String.format("%.1f", pf.smin);
            applet.text(label, Math.round(0), Math.round(applet.height + 20));
            label = String.format("%.1f", pf.smax);
            applet.text(label, Math.round(applet.width), Math.round(applet.height + 20));
        }
    }

    private void setTarget(float newTarget) {
        if (config.trajectory) {
            float current = Client.state.getMyStrategy()[0];
            if (newTarget == current) {
                Client.state.target[0] = newTarget;
            } else if (newTarget > current) {
                Client.state.target[0] = 1f;
            } else if (newTarget < current) {
                Client.state.target[0] = 0f;
            }
        } else {
            Client.state.target[0] = newTarget;
        }
    }
    
        public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (enabled) {
            boolean button = false;
            if (config.payoffFunction.getNumStrategies() == 2) {
                float x = e.getX();
                float y = e.getY();
                if (x >= width - 100 && x <= width && y >= -30 && y <= -5) {
                    Client.state.target[1] = 0;
                    button = true;
                }
                if (x >= width - 205 && x <= width - 105 && y >= -30 && y <= -5) {
                    Client.state.target[1] = 1;
                    button = true;
                }
            }
            if (!button) {
                slider.grabGhost();
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (enabled) {
            if (slider.isGhostGrabbed()) {
                slider.releaseGhost();
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (enabled && e.isActionKey()) {
            float newTarget = slider.getGhostValue();
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                if (config.trajectory) {
                    newTarget = 1f;
                } else {
                    float grid = config.grid;
                    if (Float.isNaN(grid)) {
                        grid = 0.01f;
                    }
                    newTarget += grid;
                }
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                if (config.trajectory) {
                    newTarget = 0f;
                } else {
                    float grid = config.grid;
                    if (Float.isNaN(grid)) {
                        grid = 0.01f;
                    }
                    newTarget -= grid;
                }
            }
            newTarget = Client.constrain(newTarget, 0, 1);
            setTarget(newTarget);
        }
    }

    public void keyReleased(KeyEvent e) {
        if (enabled && e.isActionKey() && (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT)) {
            setTarget(Client.state.getMyStrategy()[0]);
        }
    }
}