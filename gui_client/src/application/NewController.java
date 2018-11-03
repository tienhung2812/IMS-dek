package application;

import java.awt.Button;
import java.awt.List;
import java.awt.TextField;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.security.acl.Group;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class NewController extends Application implements Initializable {
	
	@FXML
	private TextArea textarea;
	
	@FXML
	private javafx.scene.control.Button start, deregister, Add, Remove ;
	
	@FXML
	private javafx.scene.control.TextField username;
	
	@FXML
	private javafx.scene.control.PasswordField password;
	
	@FXML
	private TableView<User> tableview = new TableView<>();
	
	@FXML
	private TableColumn<User, String> colUsername;
	
	@FXML
	private TableColumn<User, String> colURI;
	
	@FXML
	private ObservableList<User> datalist = FXCollections.observableArrayList();
	
	@FXML
	private javafx.scene.control.TextField ContactUsername, ContactURI;

	@FXML
	private void setTextArea(String string)
	{
		textarea.setText(string +"\n");
	}

	@FXML
    private void ReadCSV() // read file csv
    {
        String CsvFile = "C:\\Users\\15510\\Desktop\\IMS-dek\\gui_client\\src\\application\\User.csv";
        String FieldDelimiter = ",";

        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(CsvFile));

            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(FieldDelimiter, -1);
                
                //if(fields[0].equals(username.getText()))
                //{
	                User user = new User(fields[0], fields[1]);
	                datalist.add(user);
                //}

            }
            
        } catch (IOException ex) {
            Logger.getLogger(NewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
	
	@FXML
	private void ErrorDialog(String string)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
        //alert.setContentText("Register Fail!");
		alert.setHeaderText(string);
        alert.showAndWait();
	}
	
	@FXML
	private void handleButtonStart(ActionEvent event) throws Exception { // run button Start
		
		if((username.getText().equals("Luan") && password.getText().equals("123")) || (username.getText().equals("Tien") && password.getText().equals("456")))
		{
			//an form hien tai
			Stage stage1 = (Stage)start.getScene().getWindow();
			stage1.close();
	       
			//show form moi
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Contact.fxml"));
			Parent rootContact = (Parent) fxmlLoader.load();
	        Stage stage = new Stage();
	        stage.initModality(Modality.APPLICATION_MODAL);
	        stage.initStyle(StageStyle.UNDECORATED);
	        stage.setScene(new Scene(rootContact)); 
	        stage.show();
	        
	
		}
		else
		{
			ErrorDialog("Log in fail !!!");
		}
       

	}
	
	@FXML 
	private boolean TestURI_inFileCSV( String URI) // kiem tra URI da co trong list contact chua.
	{
		for (int i = 0 ; i < datalist.size(); i++)
		{
			User u = datalist.get(i);
			if( u.getPhone().equals(ContactURI.getText()))
				return true;
		}
        return false;
	}
	
	boolean kiemtra = false;
	@FXML
    private void handleButtonAdd(ActionEvent event) throws Exception { // Button them contact
	
		kiemtra = TestURI_inFileCSV(ContactURI.getText());
		if(ContactUsername.getText().equals("") || ContactURI.getText().equals("") )
		{
			ErrorDialog("Insert Username or Password !!!");
		}
		else
		{
			if( kiemtra == true)
			{
				ErrorDialog("URI is exists !!!");
				ContactURI.clear();
				ContactUsername.clear();
			}
			else
			{
				User newuser = new User(ContactUsername.getText(), ContactURI.getText());
				datalist.add(newuser);
				tableview.setItems(datalist);
				ContactURI.clear();
				ContactUsername.clear();
			}
		}

	}
	
	@FXML
    private void handleButtonRemove(ActionEvent event) throws Exception { // Xoa contact list contact
	
		User selectedItem = tableview.getSelectionModel().getSelectedItem();
		if(selectedItem == null)
			ErrorDialog("Choice contact need delete !!!");
		else
			tableview.getItems().remove(selectedItem);
	}
	
	@FXML
	private void handleButtonDeregister(ActionEvent event) throws Exception { // button xoa dang ki -> ra man hinh register
			
			writeTooFileCSV();
			//an form hien tai
			Stage stage1 = (Stage)deregister.getScene().getWindow();
			stage1.close();
			
			//mo form moi
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Register.fxml"));
			Parent rootContact = (Parent) fxmlLoader.load();
	        Stage stage = new Stage();
	        stage.initModality(Modality.APPLICATION_MODAL);
	        stage.initStyle(StageStyle.UNDECORATED);
	        stage.setScene(new Scene(rootContact));  
	        stage.show();
	}
	
	@FXML
	
	public void writeTooFileCSV() throws Exception {
	    Writer writer = null;
	    try {
	        File file = new File("C:\\Users\\15510\\Desktop\\IMS-dek\\gui_client\\src\\application\\User.csv");
	        writer = new BufferedWriter(new FileWriter(file));
	        for (User user : datalist) {

	            String text = user.getUsername() + "," + user.getPhone() + "\n";
	            writer.write(text);
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    finally {

	        writer.flush();
	         writer.close();
	    } 
	}
	@Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
		try {
			ReadCSV(); 
			colUsername = new TableColumn("User Name");
	        colUsername.setCellValueFactory(new PropertyValueFactory<User, String>("username"));
	        colUsername.impl_setWidth(150);

	        colURI = new TableColumn("URI");
	        colURI.setCellValueFactory(new PropertyValueFactory<User, String>("phone"));
	        colURI.impl_setWidth(250);
	        

	        //su kien Edit column
	        tableview.setEditable(true);
	        colUsername.setCellFactory(TextFieldTableCell.forTableColumn());
	        colUsername.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<User,String> >() {
				
				@Override
				public void handle(CellEditEvent<User, String> t) {
					((User) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setUsername(t.getNewValue());
				}
			});
	        
	        colURI.setCellFactory(TextFieldTableCell.forTableColumn());
	        colURI.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<User,String> >() {
				
				@Override
				public void handle(CellEditEvent<User, String> t) {
					((User) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setUsername(t.getNewValue());
				}
			});
	        
	      //gan datalist vao tableview
	        tableview.setItems(datalist);
	        tableview.getColumns().addAll(colUsername, colURI);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		
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
