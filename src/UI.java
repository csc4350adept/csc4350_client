package client;

import javafx.application.Application;
import javafx.stage.Stage;

public class UI extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Login myLogin = new Login();
			myLogin.Login(primaryStage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
