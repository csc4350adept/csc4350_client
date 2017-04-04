import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;

public class Client {
	private boolean isAuthenticated;
	private String defaultSQLitePath = "db/localdb.db";
	private SQLiteInterface sqlite;
	
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
		if(dbPath.startsWith("/")) {
			dbPath = dbPath.substring(1);
		}
		System.out.println(dbPath);
		Connection dbConn = null;
		if (!f.exists()) {
			//Create SQLite database
			try {
				dbConn = DriverManager.getConnection(dbPath);
				DatabaseMetaData meta = dbConn.getMetaData();
				System.out.println("The driver name is " + meta.getDriverName());
				System.out.println("A new database has been created");
				//TODO 
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		
		} else {
			//Just set it to the current SQLite database
			try {
				dbConn = DriverManager.getConnection(dbPath);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
		this.sqlite = new SQLiteInterface(dbConn);
	}
	
	public boolean checkAuth() {
		return this.isAuthenticated;
	}
	
	public boolean authenticate(Authenticate.proto proto, String uname, String pword) throws FeedbackException {
		Authenticate a = new Authenticate(uname, pword);
		this.isAuthenticated = a.chkCreds(proto);
		if (!this.isAuthenticated) throw new FeedbackException(a.getResp());
		else return true;
	}
	
	
}
