package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.*;

public class Controller implements Initializable  {
	
	//===============================================
	//				Giao diện
	//===============================================
	@FXML
	private Button btnSubmit;
	
	@FXML 
	private JFXButton btn_hung;
	@FXML 
	private JFXButton btn_luan;
	@FXML 
	private JFXButton btn_tien;
	@FXML 
	private JFXButton btn_khanh, btn_Addcontact, btn_Addcontact2;
	@FXML
	private ImageView img_voice, img_video;
	
	@FXML
	private Pane pane_hung; 
	@FXML
	private Pane pane_luan; 
	@FXML
	private Pane pane_tien; 
	@FXML
	private Pane pane_khanh; 
	
	@FXML
	private Label lbl_name;
	
	@FXML
	private ImageView avatar;
	@FXML
	private JFXButton btn_1;
	@FXML
	private JFXButton btn_2;
	@FXML
	private Label lbl_public;
	@FXML
	private Label lbl_private;
	@FXML
	private Label lbl_pcscf;
	
	
	@FXML 
	private JFXTextField publicUserId;
	@FXML
	private JFXTextField privateUserId;
	@FXML
	private JFXTextField proxy;
	@FXML
	private JFXTextField reami;
	
	@FXML
	private JFXPasswordField password ;
	
	//===============================================
	
	
	//===============================================
	//  			26/10/2018
	//				4h36
	//==================================================
	//            Test
	//==================================================
		private javax.swing.JTextField txtPort;
		private javax.swing.JTextField txtAOR;
		private javax.swing.JTextField txtUAS;
	    private javax.swing.JLabel lblBATDAU;
	    private javax.swing.JLabel lblTRANGTHAI;
	    private javax.swing.JTextArea txtHIENTHI;
	
	
	
	public int getPort(){
        return Integer.parseInt(proxy.getText());
    }
	
	
	 public String getAOR(){
	    return txtAOR.getText();
	 }
	    
	 public String getUAS(){ 
		return txtUAS.getText();
	 }
	 
	 public void setBATDAU(String s){
	        lblBATDAU.setText(s);
	 }
	 public void setTRANGTHAI(String s){
	        lblTRANGTHAI.setText(s);
	 }
	 
	 public void displayMessage(String s){
	        txtHIENTHI.append(s);
	    }
	//===============================================
	
	
	
	
	private Listener sipListener;
	private Main main;
	 
	@FXML
	private void handleButtonSubmit(ActionEvent event) throws Exception {
//		main = new Main();
		sipListener = new Listener(main);
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Menu.fxml"));
		Parent rootContact = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("List contact");
        stage.setScene(new Scene(rootContact));  
        stage.show();
       
//        System.out.println(sipListener);
	}
	
	String path = "D:\\GUI\\gui_client\\src\\application\\images\\";
	String btn1_text = "Call";
	String btn2_text = "Cam";
	
	@FXML 
	private void handleButtonHungAction(ActionEvent event) throws Exception {
		
		lbl_name.setText("Hung");
		File file = new File(path + "hung.png");
		Image hinh = new Image(file.toURI().toString());
		avatar.setImage(hinh);
		lbl_public.setText("Name: ");
		lbl_private.setText("Date of birth: ");
		lbl_pcscf.setText("Phone: ");
		btn_Addcontact.setText(btn1_text);
		btn_Addcontact2.setText(btn2_text);
		btn_Addcontact.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e)  {
		    	try {
		    		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML_OutGoingCall.fxml"));
			        Parent rootCall = (Parent) fxmlLoader.load();
			        Stage stage = new Stage();
			        stage.initModality(Modality.APPLICATION_MODAL);
			        stage.initStyle(StageStyle.UNDECORATED);
			        stage.setTitle("Call");
			        stage.setScene(new Scene(rootCall));  
			        stage.show();
		    	} catch(Exception ex) {
		    		ex.getMessage();
		    	}
		    	
		    }
		});
		
		File file_voice = new File(path + "voice.png");
		Image hinh_voice = new Image(file_voice.toURI().toString());
		img_voice.setImage(hinh_voice);
		File file_video = new File(path + "video.png");
		Image hinh_video = new Image(file_video.toURI().toString());
		img_video.setImage(hinh_video);
		
	}
	@FXML 
	private void handleButtonLuanAction(ActionEvent event) throws Exception {
		lbl_name.setText("Luan");
		File file = new File(path + "luan.png");
		Image hinh = new Image(file.toURI().toString());
		avatar.setImage(hinh);
		lbl_public.setText("Name: ");
		lbl_private.setText("Date of birth: ");
		lbl_pcscf.setText("Phone: ");
		
		btn_Addcontact.setText(btn1_text);
		btn_Addcontact2.setText(btn2_text);
		btn_Addcontact.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e)  {
		    	try {
		    		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML_OutGoingCall.fxml"));
			        Parent rootCall = (Parent) fxmlLoader.load();
			        Stage stage = new Stage();
			        stage.initModality(Modality.APPLICATION_MODAL);
			        stage.initStyle(StageStyle.UNDECORATED);
			        stage.setTitle("Call");
			        stage.setScene(new Scene(rootCall));  
			        stage.show();
		    	} catch(Exception ex) {
		    		ex.getMessage();
		    	}
		    	
		    }
		});
		
		
		File file_voice = new File(path + "voice.png");
		Image hinh_voice = new Image(file_voice.toURI().toString());
		img_voice.setImage(hinh_voice);
		File file_video = new File(path + "video.png");
		Image hinh_video = new Image(file_video.toURI().toString());
		img_video.setImage(hinh_video);
	}	
	
	@FXML 
	private void handleButtonTienAction(ActionEvent event) throws Exception {
		lbl_name.setText("Tien");
		File file = new File(path + "tien.png");
		Image hinh = new Image(file.toURI().toString());
		avatar.setImage(hinh);
		lbl_public.setText("Name: ");
		lbl_private.setText("Date of birth: ");
		lbl_pcscf.setText("Phone: ");
		btn_Addcontact.setText(btn1_text);
		btn_Addcontact2.setText(btn2_text);
		btn_Addcontact.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e)  {
		    	try {
		    		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML_OutGoingCall.fxml"));
			        Parent rootCall = (Parent) fxmlLoader.load();
			        Stage stage = new Stage();
			        stage.initModality(Modality.APPLICATION_MODAL);
			        stage.initStyle(StageStyle.UNDECORATED);
			        stage.setTitle("Call");
			        stage.setScene(new Scene(rootCall));  
			        stage.show();
		    	} catch(Exception ex) {
		    		ex.getMessage();
		    	}
		    	
		    }
		});
		
		
		File file_voice = new File(path + "voice.png");
		Image hinh_voice = new Image(file_voice.toURI().toString());
		img_voice.setImage(hinh_voice);
		File file_video = new File(path + "video.png");
		Image hinh_video = new Image(file_video.toURI().toString());
		img_video.setImage(hinh_video);
	}
	
	@FXML 
	private void handleButtonKhanhAction(ActionEvent event) throws Exception {
		lbl_name.setText("Khanh");
		File file = new File(path + "khanh.png");
		Image hinh = new Image(file.toURI().toString());
		avatar.setImage(hinh);
		lbl_public.setText("Name: ");
		lbl_private.setText("Date of birth: ");
		lbl_pcscf.setText("Phone: ");
		btn_Addcontact.setText(btn1_text);
		btn_Addcontact2.setText(btn2_text);
		btn_Addcontact.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e)  {
		    	try {
		    		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML_OutGoingCall.fxml"));
			        Parent rootCall = (Parent) fxmlLoader.load();
			        Stage stage = new Stage();
			        stage.initModality(Modality.APPLICATION_MODAL);
			        stage.initStyle(StageStyle.UNDECORATED);
			        stage.setTitle("Call");
			        stage.setScene(new Scene(rootCall));  
			        stage.show();
		    	} catch(Exception ex) {
		    		ex.getMessage();
		    	}
		    	
		    }
		});
		
		File file_voice = new File(path + "voice.png");
		Image hinh_voice = new Image(file_voice.toURI().toString());
		img_voice.setImage(hinh_voice);
		File file_video = new File(path + "video.png");
		Image hinh_video = new Image(file_video.toURI().toString());
		img_video.setImage(hinh_video);
	}
	
	@FXML
    private void handleButtonAddContact(ActionEvent event) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML_AddContact.fxml"));
        Parent rootContact = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Add contact");
        stage.setScene(new Scene(rootContact));  
        stage.show();
    }
    
    @FXML
    private void handleButtonOkPreperences(ActionEvent event) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Menu.fxml"));
        Parent rootContact = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Menu");
        stage.setScene(new Scene(rootContact));  
        stage.show();
    }
    
    @FXML
    private void handleButtonContactMenu(ActionEvent event) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML_Contact.fxml"));
        Parent rootContact = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Menu");
        stage.setScene(new Scene(rootContact));  
        stage.show();
    }
    
    @FXML
    private void handleButtonRegister(ActionEvent event) throws Exception {
    	
    }
    
    @FXML
    private void handleButtonOutGoingCall(ActionEvent event) throws Exception {
    	
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML_OutGoingCall.fxml"));
        Parent rootCall = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Call");
        stage.setScene(new Scene(rootCall));  
        stage.show();
    }
    
    @FXML 
    private Button btnReturn,btnCancelOutGoingCall, btnCancelInComingCall,btnCancelAddContact;
    @FXML
    private void handleButtonPrevious(ActionEvent event) throws Exception {
    	 Stage stage = (Stage) btnReturn.getScene().getWindow();
    	 stage.close();
    }
    
    @FXML
    private void handleButtonCancelOutGoingCall(ActionEvent event) throws Exception {
    	 Stage stage = (Stage) btnCancelOutGoingCall.getScene().getWindow();
    	 stage.close();
    }
    
    @FXML
    private void handleButtonCancelInComingCall(ActionEvent event) throws Exception {
    	 Stage stage = (Stage) btnCancelInComingCall.getScene().getWindow();
    	 stage.close();
    }
    
    @FXML
    private void handleButtonCancelAddContact(ActionEvent event) throws Exception {
    	 Stage stage = (Stage) btnCancelAddContact.getScene().getWindow();
    	 stage.close();
    	 
    }
    
    @FXML
    private void showAlertINFORMATION() {
        Alert alert = new Alert(AlertType.INFORMATION);
        //alert.setTitle("Test Connection");
 
        // alert.setHeaderText("Results:");
        alert.setContentText("Successfully!");
 
        alert.showAndWait();
    }
    
    @FXML
    private void showAlertERROR() {
        Alert alert = new Alert(AlertType.ERROR);
        //alert.setTitle("Test Connection");
 
        // alert.setHeaderText("Results:");
        alert.setContentText("Successfully!");
 
        alert.showAndWait();
    }
	
	
	@Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }  
}
