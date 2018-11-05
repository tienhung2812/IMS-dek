package application;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
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
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import application.NewController;

public class RegisterClass implements SipListener {
	SipFactory sipFactory;
	SipStack sipStack;
	ListeningPoint listeningPoint;
	SipProvider sipProvider;
	HeaderFactory headerFactory;
	AddressFactory addressFactory;
	MessageFactory messageFactory;

	String username="";
	String password="";
	
	String sIP;
	int iPort;
	NewController GUI;
	boolean isRegister = false;

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

	private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	public static String toHexString(byte b[]) {
		int pos = 0;
		char[] c = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			c[pos++] = toHex[(b[i] >> 4) & 0x0F];
			c[pos++] = toHex[b[i] & 0x0f];
		}
		return new String(c);
	}

	public RegisterClass(NewController gui) {
		try {
			this.GUI = gui;

			sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");

			Properties properties = new Properties();
			properties.setProperty("javax.sip.STACK_NAME", "sipStack");
			properties.setProperty("javax.sip.IP_ADDRESS", "192.168.122.231");
			properties.setProperty("javax.sip.OUTBOUND_PROXY", "192.168.122.39:5060/UDP");
			sipStack = sipFactory.createSipStack(properties);

//			sIP = InetAddress.getLocalHost().getHostAddress();
			// System.out.print(sIP);
			sIP = "192.168.122.231";

			iPort = 5060;
			String protocol = "udp";
			listeningPoint = sipStack.createListeningPoint(sIP, iPort, protocol);
			
			sipProvider = sipStack.createSipProvider(listeningPoint);
			sipProvider.addSipListener(this);
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();

			// táº¡o contact Address
			Address contactAddress = addressFactory.createAddress("sip:" + sIP + ":" + iPort);
			contactHeader = headerFactory.createContactHeader(contactAddress);

			System.out.println("SIP Ä‘Ã£ Ä‘Æ°á»£c khá»Ÿi Ä‘á»™ng: " + sIP + ":" + iPort);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public void register(String un, String pw) {
		username = un;
		password = pw;
		sendRequest("REGISTER");
	}
	public void sendRequest(String typeRequest)  {
		try {
			switch (typeRequest) {

			case "REGISTER": {
				try {
				isRegister = true;
//				int tag = Math.abs((new Random()).nextInt());
				int tag = Math.abs((new Random()).nextInt());

				String userName = username;
				String nameAddress ="sip:"+userName + "@open-ims.test";
				System.out.println(userName+" "+nameAddress);
				Address fromAddress = addressFactory.createAddress(nameAddress);
				fromAddress.setDisplayName(userName);
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));
				
				
				String userNameTo = username;
				String nameAddressTo ="sip:"+userNameTo + "@open-ims.test";
				System.out.println(userNameTo+" "+nameAddressTo);
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
				System.out.println(requestURI.toString());
				Request request = this.messageFactory.createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
						fromHeader, toHeader, viaHeaders, maxForwardsHeader);
				
//				ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(1000000000);
//				expiresHeader.setExpires(1000000000);
//				request.addHeader(expiresHeader);

				request.addHeader(contactHeader);
				
				System.out.println("\n\n" + "\nRequest la: " + request.toString());
				clientTransaction = sipProvider.getNewClientTransaction(request);
				clientTransaction.sendRequest();

//				GUI.setTRANGTHAI(clientTransaction.getState().toString());
//				GUI.displayMessage("Gá»Ÿi : " + request.toString());
				break;
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}
			
			case "DEREGISTER": {
				isRegister = false;
				int tag = Math.abs((new Random()).nextInt());

				String userName = username;
				String nameAddress ="sip:"+userName + "@open-ims.test";
				System.out.println(userName+" "+nameAddress);
				Address fromAddress = addressFactory.createAddress(nameAddress);
				fromAddress.setDisplayName(userName);
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));
				
				
				String userNameTo = username;
				String nameAddressTo ="sip:"+userNameTo + "@open-ims.test";
				System.out.println(userNameTo+" "+nameAddressTo);
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

				//GUI.setTRANGTHAI(clientTransaction.getState().toString());
//				GUI.displayMessage("Send : " + request.toString());
				break;
			}

//			case "INVITE": {

//				// Ä‘á»‹a chá»‰ server
//				Address toAddress = addressFactory.createAddress(GUI.getUAS());
//				// Ä‘á»‹a chá»‰ AOR
//				Address fromAddress = addressFactory.createAddress(GUI.getAOR());
//				// táº¡o To header
//				ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
//				// táº¡o From Header
//				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "456248");
//				// táº¡o Via Header
//				ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
//				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
//				viaHeaders.add(viaHeader);
//				// táº¡o Max-Forwards header
//				MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
//				// táº¡o Call-ID header
//				CallIdHeader callIdHeader = sipProvider.getNewCallId();
//				// táº¡o CSeq header
//				CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, "INVITE");
//
//				// táº¡o Sip Request
//				URI requestURI = toAddress.getURI(); // Ä‘á»‹a chá»‰ Destination pháº£i chuyá»ƒn sang Ä‘á»‹a chá»‰ URI
//				Request request = messageFactory.createRequest(requestURI, "INVITE", callIdHeader, cSeqHeader,
//						fromHeader, toHeader, viaHeaders, maxForwardsHeader);
//
//				// bá»• sung contact Address vÃ o Request
//				request.addHeader(contactHeader);
//
//				// gá»Ÿi Request
//				clientTransaction = sipProvider.getNewClientTransaction(request);
//				clientTransaction.sendRequest();
//
//				GUI.setTRANGTHAI(clientTransaction.getState().toString());
//				GUI.displayMessage("Gá»Ÿi : " + request.toString());// hiá»ƒn thá»‹ ná»™i dung Request trong txtHIENTHI
//				break;
//			}

//			case Request.BYE: {
//
//				// láº¥y ra Dialog Ä‘ang tá»“n táº¡i phÃ­a UAC
//				Dialog dialog = clientTransaction.getDialog();
//
//				// táº¡o 1 BYE request
//				Request ByeRequest = dialog.createRequest("BYE");
//
//				// thÃªm contactHeader Ä‘áº¿n BYE request
//				ByeRequest.addHeader(contactHeader);
//
//				// táº¡o ra 1 ClientTransaction má»›i Ä‘á»ƒ gá»Ÿi BYE request
//				BYEClientTransaction = sipProvider.getNewClientTransaction(ByeRequest);
//				dialog.sendRequest(BYEClientTransaction);
//
//				//GUI.setTRANGTHAI(BYEClientTransaction.getState().toString());
//				GUI.displayMessage("Send : " + ByeRequest.toString());
//
//			}
			}
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
}

	public void sendResponse(String typeRequest, Response response) {
		try {

			switch (typeRequest) {

			case "REGISTER": {
				isRegister = true;
				int tag = Math.abs((new Random()).nextInt());

				String userName = username;
				String nameAddress ="sip:"+userName + "@open-ims.test";
				System.out.println(userName+" "+nameAddress);
				Address fromAddress = addressFactory.createAddress(nameAddress);
				fromAddress.setDisplayName(userName);
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));
				
				
				String userNameTo = username;
				String nameAddressTo ="sip:"+userNameTo + "@open-ims.test";
				System.out.println(userNameTo+" "+nameAddressTo);
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

				URI requestURI = toAddress.getURI();
				
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
				//GUI.setTRANGTHAI(clientTransaction.getState().toString());
//				GUI.displayMessage("Send : " + request.toString());// hiá»ƒn thá»‹ ná»™i dung Request trong txtHIENTHI
				break;
			}
			
			case "DEREGISTER": {
				isRegister = false;
				int tag = Math.abs((new Random()).nextInt());

				String userName = username;
				String nameAddress ="sip:"+userName + "@open-ims.test";
				System.out.println(userName+" "+nameAddress);
				Address fromAddress = addressFactory.createAddress(nameAddress);
				fromAddress.setDisplayName(userName);
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));
				
				
				String userNameTo = username;
				String nameAddressTo ="sip:"+userNameTo + "@open-ims.test";
				System.out.println(userNameTo+" "+nameAddressTo);
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

				//GUI.setTRANGTHAI(clientTransaction.getState().toString());
//				GUI.displayMessage("Send : " + request.toString());// hiá»ƒn thá»‹ ná»™i dung Request trong txtHIENTHI
				break;
			}

//			case "INVITE": {
//
//				break;
//			}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// phÃ­a server
	public void processRequest(RequestEvent requestEvent) {
		try {
			Request request = requestEvent.getRequest();
//			GUI.displayMessage("Nháº­n : " + request.toString());

			if (request.getMethod().equals("REGISTER")) {
				// táº¡o 180 Ringing
				Response response = messageFactory.createResponse(180, request);
				// bo sung gia tri tag den ToHeader
				ToHeader toHeader = (ToHeader) response.getHeader("To");
				toHeader.setTag("453248");
				response.addHeader(contactHeader);

				serverTransaction = sipProvider.getNewServerTransaction(request);
				serverTransaction.sendResponse(response);

				//GUI.setTRANGTHAI(serverTransaction.getState().toString());
//				GUI.displayMessage("Gá»Ÿi : " + response.toString());
				System.out.println("Ä�Ã£ vao register cua server");
			}
			// khi request nháº­n Ä‘Æ°á»£c lÃ  INVITE
			else if (request.getMethod().equals("INVITE")) {
				// táº¡o 180 Ringing
				Response response = messageFactory.createResponse(180, request);
				// bo sung gia tri tag den ToHeader
				ToHeader toHeader = (ToHeader) response.getHeader("To");
				toHeader.setTag("453248");
				response.addHeader(contactHeader);

				serverTransaction = sipProvider.getNewServerTransaction(request);
				serverTransaction.sendResponse(response);

				//GUI.setTRANGTHAI(serverTransaction.getState().toString());
//				GUI.displayMessage("Send : " + response.toString());

			} // khi request nháº­n Ä‘Æ°á»£c lÃ  ACK
			else if (request.getMethod().equals("ACK")) {
				//GUI.setTRANGTHAI(serverTransaction.getState().toString());

			} // khi request nháº­n Ä‘Æ°á»£c lÃ  BYE
			else if (request.getMethod().equals("BYE")) {
				Response response = messageFactory.createResponse(200, request);
				response.addHeader(contactHeader);

				// táº¡o ra 1 ServerTransaction má»›i Ä‘á»ƒ gá»Ÿi response
				BYEServerTransaction = requestEvent.getServerTransaction();
				BYEServerTransaction.sendResponse(response);
				//GUI.setTRANGTHAI(BYEServerTransaction.getState().toString());
//				GUI.displayMessage("Send : " + response.toString());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// phÃ­a client
//	@SuppressWarnings("deprecation")
	public String info="";
	public void processResponse(ResponseEvent responseEvent) {
		try {
			Response response = responseEvent.getResponse();

//			GUI.displayMessage("Recieved : " + response.toString());
			System.out.println("Recieved : " +response.toString());
			// láº¥y CSeqHeader Ä‘Æ°á»£c dÃ­nh kÃ¨m theo trong response
			CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader("CSeq");

						
			if (cSeqHeader.getMethod().equals("REGISTER")) {
				if (response.getStatusCode() == 401) {
					if (isRegister == true) {
					sendResponse("REGISTER", response);
					}
					else {
						sendResponse("DEREGISTER", response);
					}
					info = "REGISTER-401";
				}
				// kiá»ƒm tra StatusCode cá»§a response cÃ³ pháº£i lÃ  200
				else if (response.getStatusCode() == 200) {
					System.out.println("Login successful");
					
//					Request ackRequest = clientTransaction.createAck();
//					Dialog dialog = clientTransaction.getDialog();
//					dialog.sendAck(ackRequest);
					//GUI.changeStage();
					info = "REGISTER-200";
//					GUI.displayMessage("Gá»Ÿi : " + ackRequest.toString());
				}else if(response.getStatusCode() == 403) {
					GUI.ErrorDialog("Invalid user name or password");
				}else if(response.getStatusCode() == 500) {
					GUI.ErrorDialog("Error:"+response.getStatusCode());
				}
				//GUI.setTRANGTHAI(clientTransaction.getState().toString());
			}

			if (cSeqHeader.getMethod().equals("INVITE")) {

				// kiá»ƒm tra StatusCode cá»§a response cÃ³ pháº£i lÃ  200
				if (response.getStatusCode() == 200) {
					Request ackRequest = clientTransaction.createAck();
					Dialog dialog = clientTransaction.getDialog();
					dialog.sendAck(ackRequest);

//					GUI.displayMessage("Gá»Ÿi : " + ackRequest.toString());
				}
				//GUI.setTRANGTHAI(clientTransaction.getState().toString());
			}
			// =====================

			// náº¿u kiá»ƒu request lÃ  BYE
			if (cSeqHeader.getMethod().equals("BYE")) {
				//GUI.setTRANGTHAI(BYEClientTransaction.getState().toString());
			}
			// ======================

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void processTimeout(TimeoutEvent timeoutEvent) {
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
	}

	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
	}

	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
	}

}

