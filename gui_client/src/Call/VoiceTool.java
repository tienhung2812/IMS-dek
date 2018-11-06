/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proxy;

import com.sun.media.rtp.RTPSessionMgr;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;

/**
 *
 * @author KhangDang
 */
public class VoiceTool implements ReceiveStreamListener {

    private Processor processor;
    private DataSource outDataSource;

    private RTPSessionMgr voiceSession;
    private SendStream sendStream;

    private ReceiveStream receiveStream;
    private Player player;

    private SdpInfo senderInfo;
    private SdpInfo receiverInfo;

    public void senderInfo(SdpInfo senderInfo) {
        this.senderInfo = senderInfo;
    }

    public void receiverInfo(SdpInfo receiverInfo) {
        this.receiverInfo = receiverInfo;
    }

    public void init() {
        try {
            voiceSession = new RTPSessionMgr();
            voiceSession.addReceiveStreamListener(this);

            SessionAddress localSessionAddress = new SessionAddress(
                    InetAddress.getByName(senderInfo.getIpSender()), senderInfo.getVoicePort());

            SessionAddress revSessionAddress = new SessionAddress(
                    InetAddress.getByName(receiverInfo.getIpSender()), receiverInfo.getVoicePort());

            voiceSession.initSession(new SessionAddress(), null, 0.25, 0.5);
            voiceSession.startSession(localSessionAddress, localSessionAddress, revSessionAddress, null);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void startMedia() {
        try {
            // bắt media stream từ soundcard
            MediaLocator locator = new MediaLocator("javasound://44100");

            DataSource dataSource = Manager.createDataSource(locator);

            // media stream được truyền qua mạng và loại media là RAW_RTP 
            ContentDescriptor outputFile = new ContentDescriptor(ContentDescriptor.RAW_RTP);

            // kiểu định dạng audio
            AudioFormat[] aFormat = new AudioFormat[1];
            aFormat[0] = new AudioFormat(AudioFormat.LINEAR);

            // tạo đối tượng Processor
            ProcessorModel processorModel = new ProcessorModel(dataSource, aFormat, outputFile);
            processor = Manager.createRealizedProcessor(processorModel);

            // tạo DataSource đầu ra
            outDataSource = processor.getDataOutput();

        } catch (Exception e) {
            System.out.println("error : " + e.getMessage());
        }
    }

    public void send() {
        try {
            sendStream = voiceSession.createSendStream(outDataSource, 0);
            sendStream.start();
            processor.start();

        } catch (Exception e) {
            System.out.println("error : " + e.getMessage());
        }
    }

    public void stopMedia() {
        try {

            player.stop();
            player.deallocate();
            player.close();

            sendStream.stop();

            processor.stop();
            processor.deallocate();
            processor.close();

            voiceSession.closeSession();
            voiceSession.dispose();
        } catch (Exception e) {
            System.out.println("StopMedia : " + e.getMessage());
        }
    }

    @Override
    public void update(ReceiveStreamEvent rse) {
        try {
            if (rse instanceof NewReceiveStreamEvent) {
                receiveStream = rse.getReceiveStream();
                DataSource myDs = receiveStream.getDataSource();

                player = Manager.createRealizedPlayer(myDs);
                player.start();

            }
        } catch (Exception e) {
            System.out.println("Update : " + e.getMessage());
        }
    }

}
