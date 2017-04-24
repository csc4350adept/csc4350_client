package client;
	
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.*;

public class Login{
	public void Login(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("Sign in to view your emails");
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));

			Scene scene = new Scene(grid, 450, 200);
			
			Text scenetitle = new Text("Welcome");
			scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));

			Label userName = new Label("User Name:");
			TextField userName_Field = new TextField();

			Label pw = new Label("Password:");
			PasswordField password_Field = new PasswordField();
			
			Button submitCredentials = new Button("submit");
			submitCredentials.setOnAction(new EventHandler<ActionEvent>() {
			    @Override public void handle(ActionEvent e) {
			        Boolean authenticated = false;
			    	submitCredentials.setDisable(true);
			    	if (userName_Field.getText().length() < 1) {
			    		Alert userNameAlert = new Alert(AlertType.ERROR);
			    		userNameAlert.setTitle("Login error");
			    		userNameAlert.setHeaderText("Please enter a username");
			    		userNameAlert.setContentText("Error: missing username.");
			    	
			    		userNameAlert.showAndWait();
			    	} else if (password_Field.getText().length() < 1) {
				    		Alert pwAlert = new Alert(AlertType.ERROR);
				    		pwAlert.setTitle("Login error");
				    		pwAlert.setHeaderText("Please enter a password");
				    		pwAlert.setContentText("Error: missing password.");

				    		pwAlert.showAndWait();
			    	} else {
				        submitCredentials.setText("Validating...");
				        
						try {
							if (Main.coreClient.authenticate(userName_Field.getText(), password_Field.getText())) {
								authenticated = true;
							}
						} catch (Exception AuthenticateE) {
							System.out.println(AuthenticateE.getMessage());
						} finally {
							if (authenticated) {
								try {
									Main.coreClient.update();
								} catch (ClientRequestException e1) {
									e1.printStackTrace();
								}
								try {
									Stage inboxStage = new Stage();
											
							        primaryStage.getScene().getWindow().hide();
									
							        inboxStage = Inbox.ReturnInbox();
							        
							        inboxStage.show();
									 	
						        }
						        catch (Exception e3) {
						            e3.printStackTrace();
						        }
							}
						}
			    	}
			    }
			});
			
			grid.add(scenetitle, 0, 0, 2, 1);
			grid.add(userName, 0, 1);
			grid.add(userName_Field, 1, 1);
			grid.add(pw, 0, 2);
			grid.add(password_Field, 1, 2);
			grid.add(submitCredentials, 0, 3, 2, 1);
			
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
