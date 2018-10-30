package application;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

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
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import javafx.stage.Stage;

public class Listener implements SipListener {

	// ==================================================================
	// Variables
	// ==================================================================
	SipFactory sipFactory;
	SipStack sipStack;
	ListeningPoint listeningPoint;
	SipProvider sipProvider;
	HeaderFactory headerFactory;
	AddressFactory addressFactory;
	MessageFactory messageFactory;
	Stage stage;

	String sIP;
	int iPort;
	
	Main MAIN; //Tương ứng gọi GUI
	

	ContactHeader contactHeader;

	ServerTransaction serverTransaction;
	ClientTransaction clientTransaction;
	ServerTransaction BYEServerTransaction;
	ClientTransaction BYEClientTransaction;

	// ==================================================================
	public Listener(Main main) {
		try {

			this.MAIN = main;

			sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");

			Properties properties = new Properties();
			properties.setProperty("javax.sip.STACK_NAME", "sipStack");
			sipStack = sipFactory.createSipStack(properties);

			// sIP = InetAddress.getLocalHost().
			// System.out.print(sIP);
			sIP = "192.168.122.167";
			System.out.println("IP dang hien hanh la: " + sIP);
			iPort = main.getPort();
			
			System.out.println("Port dang hien hanh la: " + iPort);
			String protocol = "udp";
			listeningPoint = sipStack.createListeningPoint(sIP, iPort, protocol);

			sipProvider = sipStack.createSipProvider(listeningPoint);
			sipProvider.addSipListener(this);
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();

			Address contactAddress = addressFactory.createAddress("sip: " + sIP + ":" + iPort);
			contactHeader = headerFactory.createContactHeader(contactAddress);
			
			

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	// ==================================================================
	// Implements functionalities
	// ==================================================================
	public void sendRequest(String typeRequest) {
		try {
			switch (typeRequest) {
			case Request.REGISTER: {
				int tag = (new Random()).nextInt();

				// địa chỉ UAS
				Address desAddress = addressFactory.createAddress(MAIN.getUAS());
				// địa chỉ AOR
				Address AORAddress = addressFactory.createAddress(MAIN.getAOR());

				// tạo From Header
				FromHeader fromHeader = headerFactory.createFromHeader(AORAddress, String.valueOf(tag));
				// tạo To header
				ToHeader toHeader = headerFactory.createToHeader(desAddress, null);
				// tạo Via Header
				ViaHeader viaHeader = headerFactory.createViaHeader(sIP, iPort, "udp", null);
				ArrayList viaHeaders = new ArrayList();
				viaHeaders.add(viaHeader);
				// The "Max-Forwards" header.
				MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);
				// The "Call-Id" header.
				CallIdHeader callIdHeader = this.sipProvider.getNewCallId();
				// The "CSeq" header.
				CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L, "REGISTER");

				// tạo Sip Request
				URI requestURI = desAddress.getURI(); // địa chỉ Destination phải chuyển sang địa chỉ URI
				System.out.println(requestURI);
				Request request = this.messageFactory.createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
						fromHeader, toHeader, viaHeaders, maxForwardsHeader);
				// Add the "Contact" header to the request.
				request.addHeader(contactHeader);

				// gởi Request
				clientTransaction = sipProvider.getNewClientTransaction(request);
				clientTransaction.sendRequest();

				MAIN.setTRANGTHAI(clientTransaction.getState().toString());
				MAIN.displayMessage("Gởi : " + request.toString());// hiển thị nội dung Request trong txtHIENTHI
				break;
			}

			case Request.INVITE: {
				// địa chỉ server
				Address desAddress = addressFactory.createAddress(MAIN.getUAS());
				// địa chỉ AOR
				Address AORAddress = addressFactory.createAddress(MAIN.getAOR());
				// tạo To header
				ToHeader toHeader = headerFactory.createToHeader(desAddress, null);
				// tạo From Header
				FromHeader fromHeader = headerFactory.createFromHeader(AORAddress, "456248");
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
				URI requestURI = desAddress.getURI(); // địa chỉ Destination phải chuyển sang địa chỉ URI
				Request request = messageFactory.createRequest(requestURI, "INVITE", callIdHeader, cSeqHeader,
						fromHeader, toHeader, viaHeaders, maxForwardsHeader);

				// bổ sung contact Address vào Request
				request.addHeader(contactHeader);

				// gởi Request
				clientTransaction = sipProvider.getNewClientTransaction(request);
				clientTransaction.sendRequest();

				MAIN.setTRANGTHAI(clientTransaction.getState().toString());
				MAIN.displayMessage("Gởi : " + request.toString());// hiển thị nội dung Request trong txtHIENTHI
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

				MAIN.setTRANGTHAI(BYEClientTransaction.getState().toString());
				MAIN.displayMessage("Gởi : " + ByeRequest.toString());
			}
			}
		} catch (Exception ex) {
			System.out.println("Send request: " + ex.getMessage());
		}
	}

	public void sendResponse() {
		try {
			// tạo response 200 OK
			Request request = serverTransaction.getRequest();
			Response response = messageFactory.createResponse(200, request);
			response.addHeader(contactHeader);

			serverTransaction.sendResponse(response);
			MAIN.setTRANGTHAI(serverTransaction.getState().toString());
			MAIN.displayMessage("Gởi : " + response.toString());

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public void processRequest(RequestEvent requestEvent) {
		try {
			// lấy SIP Request mà server nhận được
			Request request = requestEvent.getRequest();
			// hiển thị nội dung Request trong txtHIENTHI
			MAIN.displayMessage("Nhận : " + request.toString());

			// khi request nhận được là INVITE
			if (request.getMethod().equals("INVITE")) {
				// tạo 180 Ringing
				Response response = messageFactory.createResponse(180, request);
				// bo sung gia tri tag den ToHeader
				ToHeader toHeader = (ToHeader) response.getHeader("To");
				toHeader.setTag("453248");
				response.addHeader(contactHeader);

				serverTransaction = sipProvider.getNewServerTransaction(request);
				serverTransaction.sendResponse(response);

				MAIN.setTRANGTHAI(serverTransaction.getState().toString());
				MAIN.displayMessage("Gởi : " + response.toString());

			} // khi request nhận được là ACK
			else if (request.getMethod().equals("ACK")) {
				MAIN.setTRANGTHAI(serverTransaction.getState().toString());

			} // khi request nhận được là BYE
			else if (request.getMethod().equals("BYE")) {
				Response response = messageFactory.createResponse(200, request);
				response.addHeader(contactHeader);

				// tạo ra 1 ServerTransaction mới để gởi response
				BYEServerTransaction = requestEvent.getServerTransaction();
				BYEServerTransaction.sendResponse(response);
				MAIN.setTRANGTHAI(BYEServerTransaction.getState().toString());
				MAIN.displayMessage("Gởi : " + response.toString());
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public void processResponse(ResponseEvent responseEvent) {
		try {
			Response response = responseEvent.getResponse();
			MAIN.displayMessage("Nhận : " + response.toString());
			// lấy CSeqHeader được dính kèm theo trong response
			CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader("CSeq");

			// =========================
			// nếu kiểu request là REGISTER
			// =========================
			if (cSeqHeader.getMethod().equals("REGISTER")) {

				// kiểm tra StatusCode của response có phải là 200
				if (response.getStatusCode() == 200) {
					Request ackRequest = clientTransaction.createAck();
					Dialog dialog = clientTransaction.getDialog();
					dialog.sendAck(ackRequest);

					MAIN.displayMessage("Gởi : " + ackRequest.toString());
				}
				MAIN.setTRANGTHAI(clientTransaction.getState().toString());
			}
			// =====================

			// =========================
			// nếu kiểu request là INVITE
			// =========================
			if (cSeqHeader.getMethod().equals("INVITE")) {

				// kiểm tra StatusCode của response có phải là 200
				if (response.getStatusCode() == 200) {
					Request ackRequest = clientTransaction.createAck();
					Dialog dialog = clientTransaction.getDialog();
					dialog.sendAck(ackRequest);

					MAIN.displayMessage("Gởi : " + ackRequest.toString());
				}
				MAIN.setTRANGTHAI(clientTransaction.getState().toString());
			}
			// =====================
			// nếu kiểu request là BYE
			if (cSeqHeader.getMethod().equals("BYE")) {
				MAIN.setTRANGTHAI(BYEClientTransaction.getState().toString());
			}
			// ======================
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
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

	// ==================================================================
	// ==================================================================

}
