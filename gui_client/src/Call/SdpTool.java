/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proxy;

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

/**
 *
 * @author KhangDang
 */
public class SdpTool {

    private SdpFactory sdpFactory;
    
    // senderInfo chứa thông tin trong SDP message gởi đi.
    private SdpInfo senderInfo;
    // receiverInfo chứa thông tin trong SDP message nhận được.
    private SdpInfo receiverInfo;

    public SdpTool() {
        sdpFactory = SdpFactory.getInstance();
    }

    public byte[] createSdp(SdpInfo senderInfo) {
        try {
            // lưu lại thông tin người gởi trong biến senderInfo
            this.senderInfo = senderInfo;

            // tạo v-line
            Version version = sdpFactory.createVersion(0);
            
            // tạo o-line
            long ss = sdpFactory.getNtpTime(new Date());
            Origin origin = sdpFactory.createOrigin("-", ss, ss, "IN", "IP4", senderInfo.getIpSender());
            
            // tạo s-line
            SessionName sessionName = sdpFactory.createSessionName("-");
   
            // tạo t-line
            Time time = sdpFactory.createTime();
            Vector timeVector = new Vector();
            timeVector.add(time);

            // Định nghĩa media format
            int[] audioformat = new int[1];
            audioformat[0] = senderInfo.getVoiceFormat();

            // tạo m-line
            MediaDescription audioMediaDescription = sdpFactory.createMediaDescription("audio", senderInfo.getVoicePort(), 1, "RTP/AVP", audioformat);

            Vector mediaDescriptionVector = new Vector();
            mediaDescriptionVector.add(audioMediaDescription);

            // tạo SDP message 
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

            // tái tạo lại SDP message bằng JAIN SDP
            SessionDescription recSdp = sdpFactory.createSessionDescription(new String(content));

            // lấy ra địa chỉ IP người gởi trong o-line
            String myIpAddress = recSdp.getOrigin().getAddress();
            receiverInfo.setIpSender(myIpAddress);

            Vector recMediaDescriptionVector = recSdp.getMediaDescriptions(false);
            // lấy ra m-line
            MediaDescription myAudioDescription = (MediaDescription) recMediaDescriptionVector.elementAt(0);
             // lấy ra port trong m-line
            int voicePort = myAudioDescription.getMedia().getMediaPort();
            receiverInfo.setVoicePort(voicePort);

             // lấy ra media format trong m-line
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
