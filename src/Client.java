import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;

public class Client {
	private boolean isAuthenticated;
	private String defaultSQLitePath = "db/localdb.db";
	private Connection dbConn;
	
	public Client() {
		this.isAuthenticated = false;
		//Check to see if the SQLite db exists
		//If it does not exist, create it
		String path = Client.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = "";
		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.out.println("Shit");
			e.printStackTrace();
		}
		File dir = new File(String.join("/", new String[]{decodedPath, defaultSQLitePath.split("/")[0]}));
		dir.mkdir();
		System.out.println(dir.exists());
		File f = new File(decodedPath + defaultSQLitePath);
		String dbPath = "jdbc:sqlite:" + decodedPath + defaultSQLitePath;
		dbPath = "jdbc:sqlite:C:/Users/ebull_000/Documents/GitHub/csc4350_client/bin/db/localdb.db";
		System.out.println(dbPath);
		if (!f.exists()) {
			//Create SQLite database
			try {
				this.dbConn = DriverManager.getConnection(dbPath);
				DatabaseMetaData meta = this.dbConn.getMetaData();
				System.out.println("The driver name is " + meta.getDriverName());
				System.out.println("A new database has been created");
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		
		} else {
			//Just set it to the current SQLite database
			try {
				this.dbConn = DriverManager.getConnection(dbPath);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public boolean checkAuth() {
		return this.isAuthenticated;
	}
	
	
}
