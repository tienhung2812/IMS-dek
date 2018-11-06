package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.NewController;
import application.User;
import client.SipClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class RegisterController {
	
	private MainController mainController;
	

	@FXML
	public TextArea textarea;

	
	@FXML
	private Button start ;
	
	@FXML
	private TextField username;
	
	public String getUserName() {
		return username.getText();
	}
	
	@FXML
	private javafx.scene.control.PasswordField password;
	
	public String getPassword() {
		return password.getText();
	}
	

	public void displayMessage(String string)
	  {
	    	  textarea.setText(string +"\n");  

	  }
	
	public void init(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	private static boolean running = true;
	public void changeStageRegister()
	  {
	    Platform.runLater(new Runnable() {
	      @Override public void run() {

	    	 // sau 3s se tu chuyen form
	    	int count = 0;
	    	while(running)
	    	{
	    		try {
	    			count++;
	    			if(count == 4)
	    				break;
	    			Thread.sleep(1000);
	    		}catch (InterruptedException e) {
					e.getMessage();
				}
	    	}
	    	
	    	
	    	//close form cu
	  		Stage stage1 = (Stage)start.getScene().getWindow();
	  		stage1.close();
	         
	  		//Mở form mới
	  		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Contact.fxml"));
	  		Parent rootContact = null;
			try {
				rootContact = (Parent) fxmlLoader.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	          Stage stage = new Stage();
	          stage.initModality(Modality.APPLICATION_MODAL);
	          stage.initStyle(StageStyle.UNDECORATED);
	          stage.setScene(new Scene(rootContact)); 
	          stage.show();    
	      }
	    });
	  }
	
	@FXML
	public void ErrorDialog(String string)
	{
		Platform.runLater(new Runnable() {
		      @Override public void run() {
		    	//Đóng form hiện tại

		  		Alert alert = new Alert(AlertType.INFORMATION);
		        //alert.setContentText("Register Fail!");
				alert.setHeaderText(string);
		        alert.showAndWait();
		      }
		    });

	}
	
	
	@FXML
	private void handleButtonStart(ActionEvent event) throws Exception { // xử lí button Start
        //sipListener.register(getUserName(),getPassword());      

	}
}
