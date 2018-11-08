package client;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

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
import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
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
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.swing.JOptionPane;

import com.sun.media.rtsp.protocol.Header;

import Call.RingTool;
import Call.SdpInfo;
import Call.SdpTool;
import Call.VoiceTool;
import application.NewController;
import gov.nist.javax.sip.header.ims.ServiceRouteHeader;
import javafx.application.Platform;

public class SipClient implements SipListener {
	SipFactory sipFactory;
	SipStack sipStack;
	ListeningPoint listeningPoint;
	SipProvider sipProvider;
	HeaderFactory headerFactory;
	AddressFactory addressFactory;
	MessageFactory messageFactory;

	static String username = "";
	static String password = "";
	static String contactWith = "";

	String sIP;
	int iPort;
	NewController GUI;
	boolean isRegister = false;
	/////////////////////
	private boolean isUAS = false; // false -> UAC, true -> UAS
	private boolean isACK = false;
	
	 private SdpTool sdpOffer;
	 private SdpTool sdpAnswer;


	 RingTool ringClient;
	 RingTool ringServer;

	 Timer timerS;


	 VoiceTool voiceClient;

	 VoiceTool voiceServer;
	 ///////////////////////////////////
	ContactHeader contactHeader;

	ServerTransaction serverTransaction;
	ClientTransaction clientTransaction;
	
	ServerTransaction BYEServerTransaction;
	ClientTransaction BYEClientTransaction;

	public AuthorizationHeader authorizationHeader(HeaderFactory headerFactory, Response response, Request request,
			String userName, String password) throws ParseException {
		WWWAuthenticateHeader ah_c = (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);

		// Authorization header we will build with response to challenge
		AuthorizationHeader ah_r = headerFactory.createAuthorizationHeader(ah_c.getScheme());
		URI request_uri = request.getRequestURI();
		String request_method = request.getMethod();
		String nonce = ah_c.getNonce();
		String algrm = ah_c.getAlgorithm();
		String realm = ah_c.getRealm();
		MessageDigest mdigest;

		try {
			mdigest = MessageDigest.getInstance(algrm);

			// A1
			String A1 = userName + ":" + realm + ":" + password;
			String HA1 = toHexString(mdigest.digest(A1.getBytes()));
			// A2
			String A2 = request_method.toUpperCase() + ":" + request_uri;
			String HA2 = toHexString(mdigest.digest(A2.getBytes()));

			// KD
			String KD = HA1 + ":" + nonce + ":" + HA2;
			String responsenew = toHexString(mdigest.digest(KD.getBytes()));

			
			ah_r.setUsername(userName);
			ah_r.setRealm(realm);
			ah_r.setNonce(nonce);
			ah_r.setURI(request_uri);
			ah_r.setAlgorithm(algrm);
			ah_r.setResponse(responsenew);

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ah_r;

	}

	private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e','f' };

	public static String toHexString(byte b[]) {
		int pos = 0;
		char[] c = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			c[pos++] = toHex[(b[i] >> 4) & 0x0F];
			c[pos++] = toHex[b[i] & 0x0f];
		}
		return new String(c);
	}

	public SipClient(NewController gui) throws ObjectInUseException {
		try {
			this.GUI = gui;
			String ipAdd = "192.168.122.167";
			sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");

			Properties properties = new Properties();
			properties.setProperty("javax.sip.STACK_NAME", "sipStack");
			properties.setProperty("javax.sip.IP_ADDRESS", ipAdd);
			properties.setProperty("javax.sip.OUTBOUND_PROXY", "192.168.122.39:5060/UDP");
		
			sipStack = sipFactory.createSipStack(properties);
			// sIP = InetAddress.getLocalHost().getHostAddress();
			System.out.print(sIP);
			sIP = ipAdd;

			iPort = 5060;
			String protocol = "udp";

			listeningPoint = sipStack.createListeningPoint(sIP, iPort, protocol);
			sipProvider = sipStack.createSipProvider(listeningPoint);

			sipProvider.addSipListener(this);
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();

			// tạo contact Address
			Address contactAddress = addressFactory.createAddress("sip:" + sIP + ":" + iPort);
			contactHeader = headerFactory.createContactHeader(contactAddress);

			sdpOffer = new SdpTool();
            sdpAnswer = new SdpTool();

            ringClient = new RingTool();
            ringServer = new RingTool();

            voiceClient = new VoiceTool();
            voiceServer = new VoiceTool();
 ////////////////////////////////////////////////           
			System.out.println("SIP đã được khởi động: " + sIP + ":" + iPort);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void register(String un, String pw) {
		Platform.runLater(new Runnable() {
		      @Override public void run() {
					username = un;
					password = pw;
					sendRequest("REGISTER");
		      }});
	}

	public boolean isUAS() {
        return isUAS;
    }
	public void getInfo(String ct) {

		contactWith = ct;
        if (isUAS()) {
            sendResponseCall();
        } 
        else {

            sendRequest("INVITE");
        }
	}
	
	 public String getDestination()
	 {
	    return "sip:"+ contactWith+ "@open-ims.test";
	 }
	    
	 public String getAOR()
	 {
	    return "sip:" + username +"@open-ims.test";
	 }
	
	 public void getCancelCall()
	 {
		 if (isUAS()) {
			 terminateResponse();
	        } 
	     else {
	         terminateRequest();
	     }
	 }
	 
	public void sendRequest(String typeRequest) {
		try {
			switch (typeRequest) {

			case "REGISTER": {
				try {
					isRegister = true;
					// int tag = Math.abs((new Random()).nextInt());
					int tag = Math.abs((new Random()).nextInt());

					String userName = username;
					String nameAddress = "sip:" + userName + "@open-ims.test";
					System.out.println(userName + " " + nameAddress);
					Address fromAddress = addressFactory.createAddress(nameAddress);
					//fromAddress.setDisplayName(userName);
					FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));

					String userNameTo = username;
					String nameAddressTo = "sip:" + userNameTo + "@open-ims.test";
					System.out.println(userNameTo + " " + nameAddressTo);
					Address toAddress = addressFactory.createAddress(nameAddressTo);
					//toAddress.setDisplayName(userNameTo);
					ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

					ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
					ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
					viaHeaders.add(viaHeader);

					MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

					CallIdHeader callIdHeader = this.sipProvider.getNewCallId();

					CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L, "REGISTER");

					URI requestURI = toAddress.getURI();
					System.out.println(requestURI.toString());
					Request request = this.messageFactory.createRequest(requestURI, "REGISTER", callIdHeader,
							cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwardsHeader);

					// ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(1000000000);
					// expiresHeader.setExpires(1000000000);
					// request.addHeader(expiresHeader);

					request.addHeader(contactHeader);

					System.out.println("\n\n" + "\nRequest la: " + request.toString());
					clientTransaction = sipProvider.getNewClientTransaction(request);
					clientTransaction.sendRequest();

					// GUI.setTRANGTHAI(clientTransaction.getState().toString());
					GUI.textarea.appendText("Gởi : " + request.toString());
					break;
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			case "DEREGISTER": {
				isRegister = false;
				int tag = Math.abs((new Random()).nextInt());

				String userName = username;
				String nameAddress = "sip:" + userName + "@open-ims.test";
				System.out.println(userName + " " + nameAddress);
				Address fromAddress = addressFactory.createAddress(nameAddress);
				fromAddress.setDisplayName(userName);
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));

				String userNameTo = username;
				String nameAddressTo = "sip:" + userNameTo + "@open-ims.test";
				System.out.println(userNameTo + " " + nameAddressTo);
				Address toAddress = addressFactory.createAddress(nameAddressTo);
				toAddress.setDisplayName(userNameTo);
				ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

				ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				viaHeaders.add(viaHeader);

				MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

				CallIdHeader callIdHeader = this.sipProvider.getNewCallId();

				CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L, "REGISTER");

				URI requestURI = toAddress.getURI();

				Request request = this.messageFactory.createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
						fromHeader, toHeader, viaHeaders, maxForwardsHeader);

				ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(0);
				request.addHeader(expiresHeader);

				request.addHeader(contactHeader);

				clientTransaction = sipProvider.getNewClientTransaction(request);
				clientTransaction.sendRequest();

				// GUI.setTRANGTHAI(clientTransaction.getState().toString());
				System.out.println("Send : " + request.toString());
				break;
			}

			case "INVITE":
				String a = getDestination();
	        	System.out.println("Des:" + a);
	        	String b = getAOR();
	        	System.out.println("AOR: " + b);
	            ringClient.playRing("file://D:\\RingServer.mp2");
	            
	            Address toAddress = addressFactory.createAddress(a);
	            ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

	            Address fromAddress = addressFactory.createAddress( b );
	            FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "564385");

	            ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
	            ArrayList viaHeaders = new ArrayList<>();
	            viaHeaders.add(viaHeader);
	           

	            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

	            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, "INVITE");

	            CallIdHeader callIdHeader = sipProvider.getNewCallId();

	            
	            //Address proxyAddress = addressFactory.createAddress("sip:tien@open-ims.test;" );
	            URI requestUri = toAddress.getURI();
	          

	            Request request = messageFactory.createRequest(requestUri, "INVITE",
	                    callIdHeader, cSeqHeader, fromHeader, toHeader,
	                    viaHeaders, maxForwardsHeader);
	            request.addHeader(contactHeader);

	            
	            
//	            RouteHeader routeHeader = headerFactory.createRouteHeader(toAddress);
//	            request.addHeader(routeHeader);
	            
//
//	            Address server = addressFactory.createAddress("sip:orig@scscf.open-ims.test;lr");
//	            RouteHeader routeHeader2 = headerFactory.createRouteHeader(server);
//	            request.addHeader(routeHeader2);
//	            
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

	            System.out.println("Send : " + request.toString());
	            break;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void sendResponseCall() {
	       try {
	           // lấy INVITE request từ serverTransaction
	           Request request = serverTransaction.getRequest();

	           // tạo ra 200 OK response
	           Response response = messageFactory.createResponse(200, request);
	           response.addHeader(contactHeader);



	           // lấy SDP message trong message body của INVITE
	           byte[] cont = (byte[]) request.getContent();
	           // lấy ra các thông tin trong SDP message này và lưu trong biến receiverInfo
	           sdpAnswer.getSdp(cont);

	           // senderInfo_UAS : chứa các thông tin để thực hiện voice chat của UAS
	           SdpInfo senderInfo_UAS = new SdpInfo();
	           senderInfo_UAS.setIpSender(sIP);
	           senderInfo_UAS.setVoicePort(40000);
	           senderInfo_UAS.setVoiceFormat(0);

	           // Định nghĩa loại nội dung dành cho message body của 200 OK response
	           // chúng ta sử dụng application/sdp.
	           ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
	           // tạo SDP message và lưu các thông tin vào biến senderInfo của sdpAnswer
	           byte[] myContent = sdpAnswer.createSdp(senderInfo_UAS);
	           // lưu SDP message vào message body của 200 OK response
	           response.setContent(myContent, contentTypeHeader);

	           // gởi response
	           serverTransaction.sendResponse(response);

	           // Stop nhạc chuông để chuẩn bị thực hiện voice chat
	           ringServer.stopRing();
	           // hủy bỏ lịch trình
	           timerS.cancel();

	           // thực hiện voice chat phía UAS :
	           // thông tin về UAS trong sdpAnswer là senderInfo
	           voiceServer.senderInfo(sdpAnswer.getSenderInfo());
	           // thông tin về UAC trong sdpAnswer là receiverInfo
	           voiceServer.receiverInfo(sdpAnswer.getReceiverInfo());
	           // khởi tạo Session
	           voiceServer.init();
	           // bắt đầu Session
	           voiceServer.startMedia();
	           // gởi send stream
	           voiceServer.send();

	           System.out.print("Send : \n" + response.toString());

	       } catch (Exception ex) {
	           System.out.println(ex.getMessage());
	       }
	   }
	public void sendResponse(String typeRequest, Response response) {
		try {

			switch (typeRequest) {

			case "REGISTER": {
				isRegister = true;
				int tag = Math.abs((new Random()).nextInt());

				String userName = username;
				String nameAddress = "sip:" + userName + "@open-ims.test";
				System.out.println(userName + " " + nameAddress);
				Address fromAddress = addressFactory.createAddress(nameAddress);
				fromAddress.setDisplayName(userName);
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));

				String userNameTo = username;
				String nameAddressTo = "sip:" + userNameTo + "@open-ims.test";
				System.out.println(userNameTo + " " + nameAddressTo);
				Address toAddress = addressFactory.createAddress(nameAddressTo);
				toAddress.setDisplayName(userNameTo);
				ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

				String password = this.password;

				ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				viaHeaders.add(viaHeader);

				MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

				CallIdHeader callIdHeader = this.sipProvider.getNewCallId();

				CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(2L, "REGISTER");

				URI requestURI = fromAddress.getURI();

				Request request = this.messageFactory.createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
						fromHeader, toHeader, viaHeaders, maxForwardsHeader);

				AuthorizationHeader authHeader = authorizationHeader(headerFactory, response, request, userName,
						password);
				request.addHeader(authHeader);

				ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(1000000000);
				expiresHeader.setExpires(1000000000);
				request.addHeader(expiresHeader);

				request.addHeader(contactHeader);

				clientTransaction = sipProvider.getNewClientTransaction(request);
				clientTransaction.sendRequest();
				System.out.println("Send : " + request.toString());
				// GUI.setTRANGTHAI(clientTransaction.getState().toString());
				GUI.textarea.appendText("Send : " + request.toString());// hiển thị nội dung Request trong txtHIENTHI
				break;
			}

			case "DEREGISTER": {
				isRegister = false;
				int tag = Math.abs((new Random()).nextInt());

				String userName = username;
				String nameAddress = "sip:" + userName + "@open-ims.test";
				System.out.println(userName + " " + nameAddress);
				Address fromAddress = addressFactory.createAddress(nameAddress);
				fromAddress.setDisplayName(userName);
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));

				String userNameTo = username;
				String nameAddressTo = "sip:" + userNameTo + "@open-ims.test";
				System.out.println(userNameTo + " " + nameAddressTo);
				Address toAddress = addressFactory.createAddress(nameAddressTo);
				toAddress.setDisplayName(userNameTo);
				ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

				ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				viaHeaders.add(viaHeader);

				MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

				CallIdHeader callIdHeader = this.sipProvider.getNewCallId();

				CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(2L, "REGISTER");

				URI requestURI = toAddress.getURI();

				Request request = this.messageFactory.createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
						fromHeader, toHeader, viaHeaders, maxForwardsHeader);

				AuthorizationHeader authHeader = authorizationHeader(headerFactory, response, request, userName,
						userName);
				request.addHeader(authHeader);

				ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(0);
				expiresHeader.setExpires(0);
				request.addHeader(expiresHeader);

				request.addHeader(contactHeader);

				clientTransaction = sipProvider.getNewClientTransaction(request);
				clientTransaction.sendRequest();

				// GUI.setTRANGTHAI(clientTransaction.getState().toString());
				System.out.println("Send : " + request.toString());// hiển thị nội dung Request trong txtHIENTHI
				break;
			}

			 case "INVITE": {
			
				 break;
			 }
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// phía server
	public void processRequest(RequestEvent requestEvent) {
		try {
			Request request = requestEvent.getRequest();
			System.out.println("Nhận : " + request.toString());

			if (request.getMethod().equals("REGISTER")) {
				// tạo 180 Ringing
				Response response = messageFactory.createResponse(180, request);
				// bo sung gia tri tag den ToHeader
				ToHeader toHeader = (ToHeader) response.getHeader("To");
				toHeader.setTag("453248");
				response.addHeader(contactHeader);

				serverTransaction = sipProvider.getNewServerTransaction(request);
				serverTransaction.sendResponse(response);

				// GUI.setTRANGTHAI(serverTransaction.getState().toString());
				GUI.textarea.appendText("Gởi : " + response.toString());
				System.out.println("Đã vao register cua server");
			}
			// khi request nhận được là INVITE
			else if (request.getMethod().equals("INVITE")) {
				ringServer.playRing("file://D\\RingClient.mp2");
				isUAS = true;
				// tạo 180 Ringing
				Response response = messageFactory.createResponse(180, request);
				// bo sung gia tri tag den ToHeader
				ToHeader toHeader = (ToHeader) response.getHeader("To");
				toHeader.setTag("453248");
				response.addHeader(contactHeader);

				serverTransaction = sipProvider.getNewServerTransaction(request);
				serverTransaction.sendResponse(response);

				TimerTask task = new TimerTask() {
	                   @Override
	                   public void run() {
	                       //this.terminateResponse();
	                   }
	               };
	               // Khởi tạo đối tượng timerS
	               timerS = new Timer("UAS không phản hồi");
	               // Lập lịch task sẽ tự động thực hiện sau 60s nữa
	               timerS.schedule(task, 60000);

	                // GUI.setTRANGTHAI(serverTransaction.getState().toString());
				System.out.println("Send : " + response.toString());

			} // khi request nhận được là ACK
			else if (request.getMethod().equals("ACK")) {
				// GUI.setTRANGTHAI(serverTransaction.getState().toString());

			} // khi request nhận được là BYE
			else if (request.getMethod().equals("BYE")) {
				Response response = messageFactory.createResponse(200, request);
				response.addHeader(contactHeader);

				// tạo ra 1 ServerTransaction mới để gởi response
				BYEServerTransaction = requestEvent.getServerTransaction();
				BYEServerTransaction.sendResponse(response);
				// GUI.setTRANGTHAI(BYEServerTransaction.getState().toString());
				System.out.println("Send : " + response.toString());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// phía client

	public SipClient getSipListener() {
		return this;
	}

	public void disconect()// dong sipStack hien tai.
	{
			this.sipProvider.removeSipListener(this);
			try {
				this.sipProvider.removeListeningPoint(listeningPoint);
			} catch (ObjectInUseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				this.sipStack.deleteListeningPoint(this.listeningPoint);
			} catch (ObjectInUseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				this.sipStack.deleteSipProvider(this.sipProvider);
			} catch (ObjectInUseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.sipStack.stop();
//		try {
//			this.sipProvider.removeSipListener(this);
//			this.sipProvider.removeListeningPoint(listeningPoint);
//			this.sipStack.deleteListeningPoint(listeningPoint);
//			this.sipStack.deleteSipProvider(this.sipProvider);
//			this.sipStack.stop();
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			System.out.println(e.getMessage());
//		}
//		this.sipStack.stop();
	}
	// @SuppressWarnings("deprecation")

	public void processResponse(ResponseEvent responseEvent) {
		try {
			Response response = responseEvent.getResponse();

			//GUI.textarea.appendText("Recieved : " + response.toString());
			System.out.println("Recieved : " + response.toString());
			// lấy CSeqHeader được dính kèm theo trong response
			CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader("CSeq");

			if (cSeqHeader.getMethod().equals("REGISTER")) {
				if (response.getStatusCode() == 401) {
					if (isRegister == true) {
						sendResponse("REGISTER", response);
					} else {
						sendResponse("DEREGISTER", response);
					}
				}
				// kiểm tra StatusCode của response có phải là 200
				else if (response.getStatusCode() == 200) {
					// System.out.println("Login successful");
					if (isRegister == true) {
						Request ackRequest = clientTransaction.createAck();
						// Dialog dialog = clientTransaction.getDialog();
						// dialog.sendAck(ackRequest);
						
						this.GUI.textarea.appendText("Gởi : " + ackRequest.toString());
						GUI.changeStageRegister();
						
					}
					else if(isRegister == false)
					{
						disconect();
					}

				} else if (response.getStatusCode() == 403) {
					disconect();
					GUI.ErrorDialog("Invalid user name or password");

				} else if (response.getStatusCode() == 500) {
					disconect();
					GUI.ErrorDialog("Error:" + response.getStatusCode());
				}

				// GUI.setTRANGTHAI(clientTransaction.getState().toString());
			}

			if (cSeqHeader.getMethod().equals("INVITE")) {

				// kiểm tra StatusCode của response có phải là 200
				if (response.getStatusCode() == 200) {
					
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

	                System.out.println("Send : " + ACK.toString());
				}
				if(response.getStatusCode() == 404) {
	                ringClient.stopRing();

	                GUI.ErrorDialog("Khong tim thay UAS can lien lac, do UAS chua duoc dang ki cho thiet bi!");
	            }

	            if (response.getStatusCode() == 486) {

	                ringClient.stopRing();

	                GUI.ErrorDialog( "UAS da tu choi cuoc goi!");
	            }
			}
			// =====================

			// nếu kiểu request là BYE
			if (cSeqHeader.getMethod().equals("BYE")) {
				disconect();
				// GUI.setTRANGTHAI(BYEClientTransaction.getState().toString());
			}
			// ======================

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void terminateRequest() {
        try {
            if (!isACK) {
                Request cancelRequest = clientTransaction.createCancel();
                ClientTransaction cancelClientTransaction
                        = sipProvider.getNewClientTransaction(cancelRequest);
                cancelClientTransaction.sendRequest();

                // UAC huy cuoc goi dang do chuong
                ringClient.stopRing();

                System.out.println("Send : " + cancelRequest.toString());
                GUI.ErrorDialog("Cuoc goi da bi huy khi ket noi CHUA duoc thiet lap voi UAS");

            } else {
            	//UAC tao BYE de ket thuc cuoc goi
                Request byeRequest
                        = clientTransaction.getDialog().createRequest(Request.BYE);

                byeRequest.addHeader(contactHeader);

                ClientTransaction byeClientTransaction
                        = sipProvider.getNewClientTransaction(byeRequest);

                clientTransaction.getDialog().sendRequest(byeClientTransaction);
                System.out.println("Send : " + byeRequest.toString());

                isACK = false;

                voiceClient.stopMedia();
                GUI.ErrorDialog("UAC da ket thuc cuoc goi voi UAS");
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

	 public void terminateResponse() {
	        try {
	            if (!isACK) {
	                Request request = serverTransaction.getRequest();

	                // "486 BUSY HERE" response de tu choi ket noi voi UAC
	                Response response = messageFactory.createResponse(486, request);
	                serverTransaction.sendResponse(response);

	               System.out.println("Send : " + response.toString());
	                ringServer.stopRing();
	                timerS.cancel();
	                GUI.ErrorDialog("UAS da tu choi ket noi voi UAC");
	            } else {
	                // UAS tao BYE de ket thuc cuoc goi
	                Request byeRequest
	                        = serverTransaction.getDialog().createRequest("BYE");
	                byeRequest.addHeader(contactHeader);

	                ClientTransaction byeclientTransaction = sipProvider.getNewClientTransaction(byeRequest);

	                serverTransaction.getDialog().sendRequest(byeclientTransaction);

	                System.out.println("Send : " + byeRequest.toString());

	                isACK = false;
	                voiceServer.stopMedia();
	                GUI.ErrorDialog("UAS da ket thuc cuoc goi voi UAC");
	            }
	            isUAS = false;

	        } catch (Exception ex) {
	            System.out.println(ex.getMessage());
	        }
	    }
	public void processTimeout(TimeoutEvent timeoutEvent) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		 throw new UnsupportedOperationException("Not supported yet.");
	}

	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
//		ClientTransaction clientTransaction = transactionTerminatedEvent.getClientTransaction();
//        System.out.println("ClientTrasaction Terminated : " + clientTransaction.getRequest());
	}

	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
//		//Xem cac Dialog da ket thuc
//		 Dialog dialog = dialogTerminatedEvent.getDialog();
//	        System.out.println("Dialog Terminated : " + dialog);
	}
	


}
