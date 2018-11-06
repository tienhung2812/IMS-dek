/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proxy;

/**
 *
 * @author KhangDang
 */
public class SdpInfo {

    private String IpSender;
    private int voicePort;
    private int voiceFormat;
    
    public SdpInfo(){
        voiceFormat = 0;
    }

    public String getIpSender() {
        return IpSender;
    }

    public void setIpSender(String IpSender) {
        this.IpSender = IpSender;
    }

    public int getVoicePort() {
        return voicePort;
    }

    public void setVoicePort(int voicePort) {
        this.voicePort = voicePort;
    }

    public int getVoiceFormat() {
        return voiceFormat;
    }

    public void setVoiceFormat(int voiceFormat) {
        this.voiceFormat = voiceFormat;
    }
}
