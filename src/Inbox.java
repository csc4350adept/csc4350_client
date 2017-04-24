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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.*;

public class Inbox{

	public static Stage ReturnInbox() throws ClientRequestException {
		BorderPane root2 = new BorderPane();
		
        Stage stage = new Stage();
        stage.setTitle("" + Main.coreClient.getUname() + "'s inbox");
        
		GridPane grid2 = new GridPane();
		
		grid2.setAlignment(Pos.CENTER);
		grid2.setHgap(10);
		grid2.setVgap(10);
		grid2.setPadding(new Insets(25, 25, 25, 25));
		
		Scene scene2 = new Scene(grid2, 1000, 300);
		
		Button newEmail = new Button("New Email");
		newEmail.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
				Stage sendStage = new Stage();
		        BorderPane root3 = new BorderPane();
		        
				sendStage.setTitle("Compose Email");
				GridPane grid3 = new GridPane();
				grid3.setAlignment(Pos.CENTER);
				grid3.setHgap(10);
				grid3.setVgap(10);
				grid3.setPadding(new Insets(25, 25, 25, 25));
				
				Scene scene3 = new Scene(grid3, 600, 300);
				
				Label ToLabel = new Label("To: ");
				TextField To_Field = new TextField();
				Label SubjectLabel = new Label("Subject: ");
				TextField Subject_Field = new TextField();
				Label BodyLabel = new Label("Body: ");
				TextField Body_Field = new TextField();
				
				grid3.add(ToLabel, 2, 0, 2, 2);
				grid3.add(To_Field, 2, 3, 2, 2);
				grid3.add(SubjectLabel, 2, 5, 2, 2);
				grid3.add(Subject_Field, 2, 8, 2, 2);
				grid3.add(BodyLabel, 2, 10, 2, 2);
				grid3.add(Body_Field, 2, 13, 2, 2);
				
				Button sendEmail = new Button("Send Email");
				sendEmail.setOnAction(new EventHandler<ActionEvent>() {
					@Override public void handle(ActionEvent e) {
				    	sendEmail.setDisable(true);
						try {
							Main.coreClient.sendEmail(
								(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date())),
								To_Field.getText(),
								Main.coreClient.getUname(),
								Subject_Field.getText(),
								Body_Field.getText()
							);
						} catch (ClientRequestException e1) {
							e1.printStackTrace();
						} finally {
							Stage closeStage = (Stage) sendEmail.getScene().getWindow();
							closeStage.close();
						}
					}
				});
				
				grid3.add(sendEmail, 2, 15, 2, 2);
				sendStage.setScene(scene3);
				sendStage.show();
			}
		});
		grid2.add(newEmail, 4, 0, 2, 2);
		

		Button updateButton = new Button("Update");
		updateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					Main.coreClient.update();
				} catch (ClientRequestException e2) {
					e2.printStackTrace();
				}
		        updateButton.getScene().getWindow().hide();
		        
		        Stage inboxStage = new Stage();
								
		        try {
					inboxStage = Inbox.ReturnInbox();
				} catch (ClientRequestException e1) {
					e1.printStackTrace();
				}
		        
		        inboxStage.show();
			}
		});
			
		grid2.add(updateButton, 0, 0, 2, 2);
		
		Label ToLabel = new Label("To");
		grid2.add(ToLabel, 0, 2, 1, 2);
		Label FromLabel = new Label("From");
		grid2.add(FromLabel, 2, 2, 1, 2);
		Label SubjectLabel = new Label("Subject");
		grid2.add(SubjectLabel, 4, 2, 1, 2);
		Label BodyLabel = new Label("Body");
		grid2.add(BodyLabel, 6, 2, 1, 2);
		
		ArrayList<String> userEmails = Main.coreClient.getAllEmailIds(Main.coreClient.getUname());
		
		for (int i = 0; i < userEmails.size(); i++) {
			System.out.println(userEmails.get(i));
			try {
				String currTo = Main.coreClient.getEmailTo("" + userEmails.get(i));
				Label currentToLabel = new Label(currTo);
				grid2.add(currentToLabel,  0,  (i*2)+3, 1, 4);

				String currFrom = Main.coreClient.getEmailFrom("" + userEmails.get(i));
				Label currentFromLabel = new Label(currFrom);
				grid2.add(currentFromLabel,  2,  (i*2)+3, 1, 4);

				String currSubject = Main.coreClient.getEmailSubject("" + userEmails.get(i));
				Label currentSubjectLabel = new Label(currSubject);
				grid2.add(currentSubjectLabel,  4,  (i*2)+3, 1, 4);

				String currBody = Main.coreClient.getEmailBody("" + userEmails.get(i));
				currBody = currBody.replace("\\n", " ").replace("\n", " ");
				Label currentBodyLabel = new Label(currBody);
				grid2.add(currentBodyLabel,  6,  (i*2)+3, 1, 4);
			} catch (Exception e3) {
				System.out.println(e3);
			}
		}
        					            
        stage.setScene(scene2);
        return stage;
	}
}
