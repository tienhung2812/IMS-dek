/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Call;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.swing.JOptionPane;

import application.NewController;

/**
 *
 * @author KhangDang
 */
public class Call implements SipListener {

    private SipFactory sipFactory;
    private SipStack sipStack;
    private ListeningPoint listeningPoint;
    private SipProvider sipProvider;
    private MessageFactory messageFactory;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private ContactHeader contactHeader;

    private ClientTransaction clientTransaction;
    private ServerTransaction serverTransaction;

    private boolean isUAS = false; // false -> UAC, true -> UAS
    private boolean isACK = false;

    private String sIP;
    private int iSipPort;
    private NewController GUI;

    private SdpTool sdpOffer;
    private SdpTool sdpAnswer;


    RingTool ringClient;
    RingTool ringServer;

    Timer timerS;


    VoiceTool voiceClient;

    VoiceTool voiceServer;

//    public HelloPhoneListener(NewController gui) {
//        try {
//            GUI = gui;
//            sIP = "192.168.122.231";
//            iSipPort = 6060;
//
//            sipFactory = SipFactory.getInstance();
//            sipFactory.setPathName("gov.nist");
//
//            Properties properties = new Properties();
//            properties.setProperty("javax.sip.STACK_NAME", "myStack");
//            sipStack = sipFactory.createSipStack(properties);
//
//            messageFactory = sipFactory.createMessageFactory();
//            headerFactory = sipFactory.createHeaderFactory();
//            addressFactory = sipFactory.createAddressFactory();
//
//            listeningPoint = sipStack.createListeningPoint(sIP, iSipPort, "udp");
//            sipProvider = sipStack.createSipProvider(listeningPoint);
//            sipProvider.addSipListener(this);
//
//            Address contactAddress = addressFactory.createAddress("sip:" + sIP + ":" + iSipPort);
//            contactHeader = headerFactory.createContactHeader(contactAddress);
//
//            sdpOffer = new SdpTool();
//            sdpAnswer = new SdpTool();
//
//            ringClient = new RingTool();
//            ringServer = new RingTool();
//
//            voiceClient = new VoiceTool();
//            voiceServer = new VoiceTool();
//
//            this.sendRegister();
//
//            GUI.setInit("Init : " + sIP + ":" + iSipPort);
//
//        } catch (Exception ex) {
//            System.out.println(ex.getMessage());
//        }
//    }

    public boolean isUAS() {
        return isUAS;
    }

    public void disconnect() {
        try {
            sipProvider.removeSipListener(this);
            sipProvider.removeListeningPoint(listeningPoint);
            sipStack.deleteListeningPoint(listeningPoint);
            sipStack.deleteSipProvider(sipProvider);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String getDestination()
    {
    	return "sip:"+ GUI.getURIContactFromTable() +"@open-ims.test";
    }
    
    public String getAOR()
    {
    	return GUI.getUserName() +"@open-ims.test";
    }
    public void sendRequest() {
        try {

            ringClient.playRing("file://D:\\RingServer.mp2");
            
            Address toAddress = addressFactory.createAddress(getDestination());
            ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

            Address fromAddress = addressFactory.createAddress(GUI.getUserName() + "<sip:" + getAOR() + ">");
            FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "564385");

            ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iSipPort, "udp", null);
            ArrayList viaHeaders = new ArrayList();
            viaHeaders.add(viaHeader);

            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(20);

            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, "INVITE");

            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            Address proxyAddress = addressFactory.createAddress("sip:" + "192.168.122.39:5060");
            URI requestUri = proxyAddress.getURI();
            Request request = messageFactory.createRequest(requestUri, "INVITE",
                    callIdHeader, cSeqHeader, fromHeader, toHeader,
                    viaHeaders, maxForwardsHeader);
            request.addHeader(contactHeader);


            RouteHeader routeHeader = headerFactory.createRouteHeader(proxyAddress);
            request.addHeader(routeHeader);

            ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(60);
            request.addHeader(expiresHeader);


            SdpInfo senderInfo_UAC = new SdpInfo();
            senderInfo_UAC.setIpSender(sIP);
            senderInfo_UAC.setVoicePort(40000); // voice port
            senderInfo_UAC.setVoiceFormat(0);


            ContentTypeHeader myContentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");

            byte[] content = sdpOffer.createSdp(senderInfo_UAC);

            request.setContent(content, myContentTypeHeader);

            clientTransaction = sipProvider.getNewClientTransaction(request);
            clientTransaction.sendRequest();

            //GUI.Display("Send : " + request.toString());

          

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void terminateRequest() {
        try {
            if (!isACK) { //khong nhan duoc ACK di

                Request cancelRequest = clientTransaction.createCancel();

                ClientTransaction cancelClientTransaction
                        = sipProvider.getNewClientTransaction(cancelRequest);
                cancelClientTransaction.sendRequest();

                ringClient.stopRing();

               // GUI.Display("Send : " + cancelRequest.toString());

            } else {

                Request byeRequest
                        = clientTransaction.getDialog().createRequest(Request.BYE);

                byeRequest.addHeader(contactHeader);

                ClientTransaction byeClientTransaction
                        = sipProvider.getNewClientTransaction(byeRequest);
                
                clientTransaction.getDialog().sendRequest(byeClientTransaction);
                //GUI.Display("Send : " + byeRequest.toString());

                isACK = false;

                voiceClient.stopMedia();
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void sendResponse() {
        try {

            Request request = serverTransaction.getRequest();

            //tao ra 200 OK response
            Response response = messageFactory.createResponse(200, request);
            response.addHeader(contactHeader);

            // lay SDP message trong message body  INVITE
            byte[] cont = (byte[]) request.getContent();
            //lay cac tin trong SDP message 
            sdpAnswer.getSdp(cont);


            SdpInfo senderInfo_UAS = new SdpInfo();
            senderInfo_UAS.setIpSender(sIP);
            senderInfo_UAS.setVoicePort(400000);
            senderInfo_UAS.setVoiceFormat(0);


            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");

            byte[] myContent = sdpAnswer.createSdp(senderInfo_UAS);

            response.setContent(myContent, contentTypeHeader);

            serverTransaction.sendResponse(response);

            ringServer.stopRing();

            timerS.cancel();

            voiceServer.senderInfo(sdpAnswer.getSenderInfo());

            voiceServer.receiverInfo(sdpAnswer.getReceiverInfo());

            voiceServer.init();

            voiceServer.startMedia();
            
            voiceServer.send();

            //GUI.Display("Send : " + response.toString());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void terminateResponse() {
        try {
            if (!isACK) {

                Request request = serverTransaction.getRequest();

                Response response = messageFactory.createResponse(486, request);
                serverTransaction.sendResponse(response);

                //GUI.Display("Send : " + response.toString());

                ringServer.stopRing();
                timerS.cancel();
            } else {
                Request byeRequest
                        = serverTransaction.getDialog().createRequest("BYE");
                byeRequest.addHeader(contactHeader);


                ClientTransaction byeclientTransaction
                        = sipProvider.getNewClientTransaction(byeRequest);

                serverTransaction.getDialog().sendRequest(byeclientTransaction);

                //GUI.Display("Send : " + byeRequest.toString());

                isACK = false;
                voiceServer.stopMedia();
            }

            // Khi káº¿t thÃºc thÃ¬ peer nÃ y khÃ´ng cÃ²n lÃ  UAS,nÃªn thiáº¿t láº­p isUAS = false
            isUAS = false;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            //GUI.Display("Received : " + request.toString());

            if (request.getMethod().equals(Request.INVITE)) {

                isUAS = true;
                // tao 180 RINGING                
                Response response = messageFactory.createResponse(180, request);
                response.addHeader(contactHeader);

                ToHeader toHeader = (ToHeader) response.getHeader("To");
                toHeader.setTag("45432678");

                serverTransaction = sipProvider.getNewServerTransaction(request);
                serverTransaction.sendResponse(response);

                ringServer.playRing("file://D:\\RingClient.mp2");

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        Call.this.terminateResponse();
                    }
                };

                timerS = new Timer("UAS khong phan hoi");

                timerS.schedule(task, 10000);

                //GUI.Display("send : " + response.toString());
            }

            if (request.getMethod().equals(Request.CANCEL)) {

                Request inviteReq = serverTransaction.getRequest();

                Response response = messageFactory.createResponse(487, inviteReq);
                serverTransaction.sendResponse(response);
                //GUI.Display("send : " + response.toString());

                // tao "200 OK" response danh cho CANCEL           
                Response cancelResponse = messageFactory.createResponse(200, request);

                ServerTransaction cancelServerTransaction = requestEvent.getServerTransaction();
                cancelServerTransaction.sendResponse(cancelResponse);
                //GUI.Display("send : " + cancelResponse.toString());

                ringServer.stopRing();
                GUI.ErrorDialog("UAC da huy cuoc goi !");

                timerS.cancel();

                isUAS = false;
            }

            if (request.getMethod().equals(Request.ACK)) {
                isACK = true;
            }

            if (request.getMethod().equals(Request.BYE)) {
                // tao"200 OK" response danh cho BYE request
                Response response = messageFactory.createResponse(200, request);
                response.addHeader(contactHeader);

                ServerTransaction byeServerTransaction = requestEvent.getServerTransaction();
                byeServerTransaction.sendResponse(response);

                if (!isUAS) {
                    voiceClient.stopMedia();
                    GUI.ErrorDialog("UAS da ket thuc cuoc goi!");
                }

                if (isUAS) {
                    voiceServer.stopMedia();
                    GUI.ErrorDialog("UAC da ket thuc cuoc goi !");
                }

                //GUI.Display("Send : " + response.toString());

                isUAS = false;

                isACK = false;
            }

        } catch (Exception ex) {
            System.out.println("processRequest : " + ex.getMessage());
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        try {
            Response response = responseEvent.getResponse();
            //GUI.Display("Received : " + response.toString());
            CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);


//            if ((cSeqHeader.getMethod().equals(Request.REGISTER))
//                    && (response.getStatusCode() == 200)) {
//                GUI.statusRegister(true);
//            }
//
//            if ((cSeqHeader.getMethod().equals(Request.REGISTER))
//                    && (response.getStatusCode() == 408)) {
//                GUI.statusRegister(false);
//            }

            if ((cSeqHeader.getMethod().equals(Request.INVITE))
                    && (response.getStatusCode() == 200)) {

                //LAY SDP message trong message body  200 OK
                byte[] content = (byte[]) response.getContent();
                sdpOffer.getSdp(content);

                long numseq = cSeqHeader.getSeqNumber();
                Request ACK = clientTransaction.getDialog().createAck(numseq);
                ACK.addHeader(contactHeader);
                clientTransaction.getDialog().sendAck(ACK);

                ringClient.stopRing();

                isACK = true;

                voiceClient.senderInfo(sdpOffer.getSenderInfo());

                voiceClient.receiverInfo(sdpOffer.getReceiverInfo());

                voiceClient.init();

                voiceClient.startMedia();

                voiceClient.send();

                //GUI.Display("Send : " + ACK.toString());

            }

            if ((cSeqHeader.getMethod().equals(Request.INVITE))
                    && (response.getStatusCode() == 404)) {

                ringClient.stopRing();

                GUI.ErrorDialog("Server khong biet contact address cua UAS !");
            }

            if (response.getStatusCode() == 486) {

                ringClient.stopRing();
                GUI.ErrorDialog("UAS da tu choi cuoc goi !");
            }

        } catch (Exception ex) {
            System.out.println("processResponse : " + ex.getMessage());
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent
    ) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent
    ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent
    ) {

        ClientTransaction clientTransaction = transactionTerminatedEvent.getClientTransaction();
        System.out.println("ClientTrasaction Terminated : " + clientTransaction.getRequest());
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent
    ) {

        Dialog dialog = dialogTerminatedEvent.getDialog();
        System.out.println("Dialog Terminated : " + dialog);
    }

}
