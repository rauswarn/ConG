/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong;

import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.server.Server;

/**
 *
 * @author jpettit
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
        Server.main(new String[]{"localhost", "localhost",
        "configs/test.csv"});
        Client.main(new String[]{"player1", "localhost"});
        Client.main(new String[]{"player2", "localhost"});
    }
}
