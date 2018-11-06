/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proxy;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.DataSource;

/**
 *
 * @author KhangDang
 */
public class RingTool {

    private Player player;

    public void playRing(String filename) {
        try {
            // tạo media locator của tập tin
            MediaLocator ml = new MediaLocator(filename);
            // play tập tin 
            player = Manager.createRealizedPlayer(ml);
            player.start();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void stopRing() {
        try {
            if (player.getState() != Player.Started) {
                Thread.sleep(1000);
                stopRing();
                return;
            }
            player.stop();
            player.close();
            player.deallocate();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
