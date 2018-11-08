/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Call;

import java.util.Date;
import java.util.Vector;
import javax.media.rtp.SessionAddress;
import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.sdp.SessionName;
import javax.sdp.Time;
import javax.sdp.Version;


public class SdpTool {

    private SdpFactory sdpFactory;
    
    // senderInfo chá»©a thÃ´ng tin trong SDP message gá»Ÿi Ä‘i.
    private SdpInfo senderInfo;
    // receiverInfo chá»©a thÃ´ng tin trong SDP message nháº­n Ä‘Æ°á»£c.
    private SdpInfo receiverInfo;

    public SdpTool() {
        sdpFactory = SdpFactory.getInstance();
    }

    public byte[] createSdp(SdpInfo senderInfo) {
        try {
            // lÆ°u láº¡i thÃ´ng tin ngÆ°á»�i gá»Ÿi trong biáº¿n senderInfo
            this.senderInfo = senderInfo;

            // táº¡o v-line
            Version version = sdpFactory.createVersion(0);
            
            // táº¡o o-line
            long ss = sdpFactory.getNtpTime(new Date());
            Origin origin = sdpFactory.createOrigin("-", ss, ss, "IN", "IP4", senderInfo.getIpSender());
            
            // táº¡o s-line
            SessionName sessionName = sdpFactory.createSessionName("-");
   
            // táº¡o t-line
            Time time = sdpFactory.createTime();
            Vector timeVector = new Vector();
            timeVector.add(time);

            // Ä�á»‹nh nghÄ©a media format
            int[] audioformat = new int[1];
            audioformat[0] = senderInfo.getVoiceFormat();

            // táº¡o m-line
            MediaDescription audioMediaDescription = sdpFactory.createMediaDescription("audio", senderInfo.getVoicePort(), 1, "RTP/AVP", audioformat);

            Vector mediaDescriptionVector = new Vector();
            mediaDescriptionVector.add(audioMediaDescription);

            // táº¡o SDP message 
            SessionDescription sdpMessage = sdpFactory.createSessionDescription();
            sdpMessage.setVersion(version);
            sdpMessage.setOrigin(origin);
            sdpMessage.setSessionName(sessionName);
            sdpMessage.setTimeDescriptions(timeVector);
            sdpMessage.setMediaDescriptions(mediaDescriptionVector);

            return sdpMessage.toString().getBytes();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public void getSdp(byte[] content) {
        try {
            receiverInfo = new SdpInfo();

            // tÃ¡i táº¡o láº¡i SDP message báº±ng JAIN SDP
            SessionDescription recSdp = sdpFactory.createSessionDescription(new String(content));

            // láº¥y ra Ä‘á»‹a chá»‰ IP ngÆ°á»�i gá»Ÿi trong o-line
            String myIpAddress = recSdp.getOrigin().getAddress();
            receiverInfo.setIpSender(myIpAddress);

            Vector recMediaDescriptionVector = recSdp.getMediaDescriptions(false);
            // láº¥y ra m-line
            MediaDescription myAudioDescription = (MediaDescription) recMediaDescriptionVector.elementAt(0);
             // láº¥y ra port trong m-line
            int voicePort = myAudioDescription.getMedia().getMediaPort();
            receiverInfo.setVoicePort(voicePort);

             // láº¥y ra media format trong m-line
            Vector voiceFormatVector = myAudioDescription.getMedia().getMediaFormats(false);
            int voiceFormat = Integer.parseInt(voiceFormatVector.elementAt(0).toString());
            receiverInfo.setVoiceFormat(voiceFormat);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public SdpInfo getSenderInfo() {
        return senderInfo;
    }
    
    public SdpInfo getReceiverInfo() {
        return receiverInfo;
    }
}
