package application;

import java.awt.Button;
import java.awt.TextField;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import client.SipClient;

public class NewController extends Application implements Initializable {
	
	@FXML
	private SipClient sipclient;
	private TextArea textarea;
	
	public void displayMessage(String message) {

		textarea.setText(message);
	}
	
	@FXML
	private javafx.scene.control.Button start, deregister ;
	
	@FXML
	private javafx.scene.control.TextField username;
	
	public String getUserName() {
		return username.getText();
	}
	
	@FXML
	private javafx.scene.control.PasswordField password;
	
	public String getPassword() {
		return password.getText();
	}
	
	@FXML
	public void changeStage() throws IOException {
		Stage stage1 = (Stage)start.getScene().getWindow();
		stage1.close();
		
		//Mở form mới
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Contact.fxml"));
		Parent rootContact = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(rootContact));  
        stage.show();
	}
	
	@FXML
	public void alert(String err) {
		Alert alert = new Alert(AlertType.ERROR);
        //alert.setContentText("Register Fail!");
		alert.setHeaderText(err);
        alert.showAndWait();
	}
	@FXML
	private void handleButtonStart(ActionEvent event) throws Exception {
		//displayMessage("sefsef");
		textarea.setText("ABC");
//		sipclient = new SipClient(this);
//		sipclient.register(username.getText(),password.getText());
//		System.out.println( password.getText() + username.getText());

	}
	
	@FXML
	private void handleButtonDeregister(ActionEvent event) throws Exception {
			//Đóng form hiện tại
			Stage stage1 = (Stage)deregister.getScene().getWindow();
			stage1.close();
			
			//Mở form mới
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Register.fxml"));
			Parent rootContact = (Parent) fxmlLoader.load();
	        Stage stage = new Stage();
	        stage.initModality(Modality.APPLICATION_MODAL);
	        stage.initStyle(StageStyle.UNDECORATED);
	        stage.setScene(new Scene(rootContact));  
	        stage.show();
	}
	
	@Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }  

	public void start(Stage stage) throws Exception {
	        Parent root = FXMLLoader.load(getClass().getResource("Register.fxml"));
	        
	        Scene scene = new Scene(root);
	        
	        stage.setScene(scene);
	        stage.show();
    }
	
	public static void main(String[] args) {
		launch(args);			
	}
}
