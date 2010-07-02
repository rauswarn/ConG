/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import java.util.ArrayList;
import java.util.List;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author alexlou
 */
public class HeatmapLegend extends Sprite {

    private HeatmapHelper heatmap;

    public HeatmapLegend(int x, int y, int width, int height) {
        super(x, Math.round(y + .05f * height), width, Math.round(.9f * height));
    }

    @Override
    public void draw(PEmbed applet) {
        if (!visible) {
            return;
        }
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        applet.strokeWeight(1f);
        heatmap = new HeatmapHelper(0, 0, 0, 0, true, applet);
        for (float y = 0; y < this.height; y++) {
            float percent = .999f - y / this.height;
            applet.stroke(heatmap.getRGB(percent));
            applet.line(0, y, (float) this.width, y);
        }
        applet.popMatrix();
    }
}