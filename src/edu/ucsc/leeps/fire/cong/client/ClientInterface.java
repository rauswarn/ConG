/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

/**
 *
 * @author jpettit
 */
public interface ClientInterface extends edu.ucsc.leeps.fire.client.ClientInterface {

    public void setStrategyAB(float A, float B, float a, float b);

    public float[] getStrategyAB();

    public void setStrategyRPS(
            float R, float P, float S);

    public void setOpponentRPS(
            float r, float p, float s);


    public float[] getStrategyRPS();
}
