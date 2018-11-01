package client;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import javax.naming.AuthenticationException;
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
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class exampleListener implements SipListener {
	SipFactory sipFactory;
	SipStack sipStack;
	ListeningPoint listeningPoint;
	SipProvider sipProvider;
	HeaderFactory headerFactory;
	AddressFactory addressFactory;
	MessageFactory messageFactory;

	String sIP;
	int iPort;
	exampleGUI GUI;

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
		userName = "tien";
		password = "tien";
		try {
			mdigest = MessageDigest.getInstance(algrm);

			// A1
			String A1 = userName + ":" + realm + ":" + password;
			System.out.println(A1);
			String HA1 = toHexString(mdigest.digest(A1.getBytes()));
			System.out.println(HA1);
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

	public exampleListener(exampleGUI gui) {
		try {
			this.GUI = gui;

			sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");

			Properties properties = new Properties();
			properties.setProperty("javax.sip.STACK_NAME", "sipStack");
			properties.setProperty("javax.sip.IP_ADDRESS", "192.168.122.230");
			properties.setProperty("javax.sip.OUTBOUND_PROXY", "192.168.122.39:5060/UDP");
			sipStack = sipFactory.createSipStack(properties);

//			sIP = InetAddress.getLocalHost().getHostAddress();
			// System.out.print(sIP);
			sIP = "192.168.122.230";

			iPort = GUI.getPort();
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

			GUI.setBATDAU("SIP đã được khởi động: " + sIP + ":" + iPort);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public void sendRequest(String typeRequest) {
		try {
			switch (typeRequest) {

			case Request.REGISTER: {
				int tag = Math.abs((new Random()).nextInt());

				String nameAddress = GUI.getAOR();
				String userName = nameAddress.substring(nameAddress.indexOf(":") + 1, nameAddress.indexOf("@"));
				Address fromAddress = addressFactory.createAddress(nameAddress);
				fromAddress.setDisplayName(userName);
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));

				nameAddress = GUI.getUAS();
				userName = nameAddress.substring(nameAddress.indexOf(":") + 1, nameAddress.indexOf("@"));
				Address toAddress = addressFactory.createAddress(nameAddress);
				toAddress.setDisplayName(userName);
				ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

				ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				viaHeaders.add(viaHeader);

				MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

				CallIdHeader callIdHeader = this.sipProvider.getNewCallId();

				CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L, "REGISTER");

				URI requestURI = toAddress.getURI();
				System.out.println(requestURI);
				Request request = this.messageFactory.createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
						fromHeader, toHeader, viaHeaders, maxForwardsHeader);

				request.addHeader(contactHeader);

				clientTransaction = sipProvider.getNewClientTransaction(request);
				clientTransaction.sendRequest();

				GUI.setTRANGTHAI(clientTransaction.getState().toString());
				GUI.displayMessage("Gởi : " + request.toString());// hiển thị nội dung Request trong txtHIENTHI
				break;
			}

			case Request.INVITE: {

				// địa chỉ server
				Address toAddress = addressFactory.createAddress(GUI.getUAS());
				// địa chỉ AOR
				Address fromAddress = addressFactory.createAddress(GUI.getAOR());
				// tạo To header
				ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
				// tạo From Header
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "456248");
				// tạo Via Header
				ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				viaHeaders.add(viaHeader);
				// tạo Max-Forwards header
				MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
				// tạo Call-ID header
				CallIdHeader callIdHeader = sipProvider.getNewCallId();
				// tạo CSeq header
				CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, "INVITE");

				// tạo Sip Request
				URI requestURI = toAddress.getURI(); // địa chỉ Destination phải chuyển sang địa chỉ URI
				Request request = messageFactory.createRequest(requestURI, "INVITE", callIdHeader, cSeqHeader,
						fromHeader, toHeader, viaHeaders, maxForwardsHeader);

				// bổ sung contact Address vào Request
				request.addHeader(contactHeader);

				// gởi Request
				clientTransaction = sipProvider.getNewClientTransaction(request);
				clientTransaction.sendRequest();

				GUI.setTRANGTHAI(clientTransaction.getState().toString());
				GUI.displayMessage("Gởi : " + request.toString());// hiển thị nội dung Request trong txtHIENTHI
				break;
			}

			case Request.BYE: {

				// lấy ra Dialog đang tồn tại phía UAC
				Dialog dialog = clientTransaction.getDialog();

				// tạo 1 BYE request
				Request ByeRequest = dialog.createRequest("BYE");

				// thêm contactHeader đến BYE request
				ByeRequest.addHeader(contactHeader);

				// tạo ra 1 ClientTransaction mới để gởi BYE request
				BYEClientTransaction = sipProvider.getNewClientTransaction(ByeRequest);
				dialog.sendRequest(BYEClientTransaction);

				GUI.setTRANGTHAI(BYEClientTransaction.getState().toString());
				GUI.displayMessage("Gởi : " + ByeRequest.toString());

			}
			}
		} catch (Exception ex) {
			System.out.println("send request : " + ex.getMessage());
		}
	}

	public void sendResponse(String typeRequest, Response response) {
		try {

			switch (typeRequest) {

			case Request.REGISTER: {
				int tag = Math.abs((new Random()).nextInt());

				String nameAddress = GUI.getAOR();
				String userName = nameAddress.substring(nameAddress.indexOf(":") + 1, nameAddress.indexOf("@"));
				Address fromAddress = addressFactory.createAddress(nameAddress);
				fromAddress.setDisplayName(userName);
				FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(tag));

				nameAddress = GUI.getUAS();
				userName = nameAddress.substring(nameAddress.indexOf(":") + 1, nameAddress.indexOf("@"));
				Address toAddress = addressFactory.createAddress(nameAddress);
				toAddress.setDisplayName(userName);
				ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

				ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				viaHeaders.add(viaHeader);

				MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

				CallIdHeader callIdHeader = this.sipProvider.getNewCallId();

				CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(2L, "REGISTER");

				URI requestURI = toAddress.getURI();
				System.out.println(requestURI);
				Request request = this.messageFactory.createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
						fromHeader, toHeader, viaHeaders, maxForwardsHeader);
				if (request != null) {
					AuthorizationHeader authHeader = authorizationHeader(headerFactory, response, request, userName,
							"123456");
					request.addHeader(authHeader);

				}
				
				request.addHeader(contactHeader);

				clientTransaction = sipProvider.getNewClientTransaction(request);
				clientTransaction.sendRequest();

				GUI.setTRANGTHAI(clientTransaction.getState().toString());
				GUI.displayMessage("Gởi : " + request.toString());// hiển thị nội dung Request trong txtHIENTHI
				break;
			}

			case Request.INVITE: {

				break;
			}
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	// phía server
	public void processRequest(RequestEvent requestEvent) {
		try {
			Request request = requestEvent.getRequest();
			GUI.displayMessage("Nhận : " + request.toString());

			if (request.getMethod().equals("REGISTER")) {
				// tạo 180 Ringing
				Response response = messageFactory.createResponse(180, request);
				// bo sung gia tri tag den ToHeader
				ToHeader toHeader = (ToHeader) response.getHeader("To");
				toHeader.setTag("453248");
				response.addHeader(contactHeader);

				serverTransaction = sipProvider.getNewServerTransaction(request);
				serverTransaction.sendResponse(response);

				GUI.setTRANGTHAI(serverTransaction.getState().toString());
				GUI.displayMessage("Gởi : " + response.toString());
				System.out.println("Đã vao register cua server");
			}
			// khi request nhận được là INVITE
			else if (request.getMethod().equals("INVITE")) {
				// tạo 180 Ringing
				Response response = messageFactory.createResponse(180, request);
				// bo sung gia tri tag den ToHeader
				ToHeader toHeader = (ToHeader) response.getHeader("To");
				toHeader.setTag("453248");
				response.addHeader(contactHeader);

				serverTransaction = sipProvider.getNewServerTransaction(request);
				serverTransaction.sendResponse(response);

				GUI.setTRANGTHAI(serverTransaction.getState().toString());
				GUI.displayMessage("Gởi : " + response.toString());

			} // khi request nhận được là ACK
			else if (request.getMethod().equals("ACK")) {
				GUI.setTRANGTHAI(serverTransaction.getState().toString());

			} // khi request nhận được là BYE
			else if (request.getMethod().equals("BYE")) {
				Response response = messageFactory.createResponse(200, request);
				response.addHeader(contactHeader);

				// tạo ra 1 ServerTransaction mới để gởi response
				BYEServerTransaction = requestEvent.getServerTransaction();
				BYEServerTransaction.sendResponse(response);
				GUI.setTRANGTHAI(BYEServerTransaction.getState().toString());
				GUI.displayMessage("Gởi : " + response.toString());
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	// phía client
//	@SuppressWarnings("deprecation")
	public void processResponse(ResponseEvent responseEvent) {
		try {
			Response response = responseEvent.getResponse();

			GUI.displayMessage("Nhận : " + response.toString());

			// lấy CSeqHeader được dính kèm theo trong response
			CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader("CSeq");

			// =========================
			// nếu kiểu request là REGISTER
			// =========================
			if (cSeqHeader.getMethod().equals("REGISTER")) {
				if (response.getStatusCode() == 401) {
					sendResponse("REGISTER", response);
				}
				// kiểm tra StatusCode của response có phải là 200
				else if (response.getStatusCode() == 200) {
					Request ackRequest = clientTransaction.createAck();
					Dialog dialog = clientTransaction.getDialog();
					dialog.sendAck(ackRequest);

					GUI.displayMessage("Gởi : " + ackRequest.toString());
				}
				GUI.setTRANGTHAI(clientTransaction.getState().toString());
			}

			if (cSeqHeader.getMethod().equals("INVITE")) {

				// kiểm tra StatusCode của response có phải là 200
				if (response.getStatusCode() == 200) {
					Request ackRequest = clientTransaction.createAck();
					Dialog dialog = clientTransaction.getDialog();
					dialog.sendAck(ackRequest);

					GUI.displayMessage("Gởi : " + ackRequest.toString());
				}
				GUI.setTRANGTHAI(clientTransaction.getState().toString());
			}
			// =====================

			// nếu kiểu request là BYE
			if (cSeqHeader.getMethod().equals("BYE")) {
				GUI.setTRANGTHAI(BYEClientTransaction.getState().toString());
			}
			// ======================

		} catch (Exception ex) {
			System.out.println("processResponse : " + ex.getMessage());
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
