import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;

public class SQLiteInterface {
	Client client;
	Connection c;
	
	public SQLiteInterface(Client client) {
		this.client = client;
		
		//Check to see if the SQLite db exists
		//If it does not exist, create it
		String path = Client.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = "";
		String defaultSQLitePath = "db/localdb.sqlite";
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
		c = dbConn;
	}
	
	public boolean mkCreds(String uname, String pword) {
		String sql = String.format("insert into USERS values(\"%s\", \"%s\")", uname, pword);
		try {
			Statement msg = c.createStatement();
			msg.executeQuery(sql);
		} catch (SQLException e) {
			if (!e.getMessage().equals("query does not return ResultSet")) return false;
		}
		System.out.println("Created credentials");
		return true;
	}
	
	public boolean chkUserExists(String uname) {
		String sql = String.format("select UNAME from USERS where UNAME = \"%s\"", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("UNAME") != null) return true;
		} catch (SQLException e) {
			return false;
		}
		return false;
	}

	public boolean chkCreds(String uname, String pword) {
		String sql = String.format("select PWORD from USERS where UNAME = \"%s\"", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("PWORD") != null && pword.equals(resp.getString("PWORD"))) { //This needs to be more secure, but for now let it be
				System.out.println("success!");
				return true;
			}
		}
		catch (SQLException e) {
			if (e.getMessage().equals("ResultSet closed")) return false;
		}
		
		return false;
	}
}
