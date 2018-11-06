package Main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import client.SipClient;


public class MainController {
	
	public SipClient sipListener;
	
	@FXML
	private RegisterController registerController;
	@FXML
	private ContactController contactController;
	
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
	
	
	@FXML private void initialize() {
		registerController.init(this);
		contactController.init(this);
	}

}
