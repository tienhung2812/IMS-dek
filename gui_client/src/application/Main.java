package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;



public class Main extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXML_Start.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
    }
	
	Controller cl = new Controller();
	
	private javax.swing.JTextField txtPort;
	private javax.swing.JTextField txtAOR;
	private javax.swing.JTextField txtUAS;
    private javax.swing.JLabel lblBATDAU;
    private javax.swing.JLabel lblTRANGTHAI;
    private javax.swing.JTextArea txtHIENTHI;
    
    
	public int getPort(){
		return cl.getPort();
//        return Integer.parseInt(txtPort.getText());
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
	 //================================================
	 //
	 //================================================
	public static void main(String[] args) {
		launch(args);
		
		
	}
}
