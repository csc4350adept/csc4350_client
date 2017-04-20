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


/*
 * Database
 * 
 * USERS
 * UNAME|PWORD|SERVER|IMAP|SMTP|KEY
 */

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
				this.init(dbConn);
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
		} catch (SQLException e) {
			if (e.getMessage().equals("ResultSet closed")) return false;
		}
		return false;
	}
	
	/*-----------------------------SETTERS-------------------------------------------------*/
	
	private void init(Connection dbc) throws SQLException {
		String sql = "create table USERS(UNAME varchar(20) null, PWORD varchar(20) null), SERVER varchar(20) null, IMAP int null, SMTP int null";
		try {
			Statement msg = dbc.createStatement();
			msg.executeQuery(sql);
		} catch (SQLException e) {
			if (!e.getMessage().equals("query does not return ResultSet")) throw e;
		}
	}
	
	public boolean mkCreds(String uname, String pword) {
		String server = client.getDefServer();
		int imap = client.getDefIMAPPort();
		int smtp = client.getDefSMTPPort();
		String key = client.getDefaultUserKey();
		return this.mkCreds(uname, pword, server, imap, smtp, key);
	}
	
	public boolean mkCreds(String uname, String pword, String server, int imap, int smtp, String key) {
		System.out.println("Creating credentials");
		String sql = String.format("insert into USERS values(\"%s\", \"%s\", \"%s\", %d, %d, \"%s\")", uname, pword, server, imap, smtp, key);
		Statement msg;
		try {
			msg = c.createStatement();
			msg.executeQuery(sql);
		} catch (SQLException e) {
			if (e.getMessage().startsWith("[SQLITE_ERROR] SQL error or missing database (no such table: USERS)")) {
				String createTable = "create table USERS(UNAME varchar(20) primary key null, PWORD varchar(20) null, SERVER varchar(15) null, IMAP INTEGER null, SMTP INTEGER null, KEY varchar(512) null)";
				try {
					msg = c.createStatement();
					msg.executeQuery(createTable);
				} catch (SQLException cte) {
					if (cte.getMessage().equals("query does not return ResultSet")) {
						try {
							msg = c.createStatement();
							msg.executeQuery(sql);
						} catch (SQLException cte2) {
							if (cte2.getMessage().equals("query does not return ResultSet")) {
								System.out.println("User created");
								return true;
							}
						}
						
					}
					return false;
				}	
			}
			if (!e.getMessage().equals("query does not return ResultSet")) return false;
		}
		System.out.println("Created credentials");
		return true;
	}
	
	public boolean setServer(String uname, String pword, String server) throws ClientRequestException {
		if (this.chkUserExists(uname)) {
			if (this.chkCreds(uname, pword)) {
				String sql = String.format("update USERS set SERVER=\"%s\" where UNAME=\"%s\"", server, uname);
				try {
					Statement msg = c.createStatement();
					msg.executeQuery(sql);
					return true;
				} catch (SQLException e) {
					if (!e.getMessage().equals("query does not return ResultSet"))
					throw new ClientRequestException("SQL Exception: " + e.getMessage());
				}
			}
		}
		return false;
	}
	
	
	
	public boolean setIMAP(String uname, String pword, int imap) throws ClientRequestException {
		if (this.chkUserExists(uname)) {
			if (this.chkCreds(uname, pword)) {
				String sql = String.format("update USERS set IMAP=\"%d\" where UNAME=\"%s\"", imap, uname);
				try {
					Statement msg = c.createStatement();
					msg.executeQuery(sql);
					return true;
				} catch (SQLException e) {
					if (!e.getMessage().equals("query does not return ResultSet"))
					throw new ClientRequestException("SQL Exception: " + e.getMessage());
				}
			}
		}
		return false;
	}
	
	public boolean setSMTP(String uname, String pword, int smtp) throws ClientRequestException {
		if (this.chkUserExists(uname)) {
			if (this.chkCreds(uname, pword)) {
				String sql = String.format("update USERS set SMTP=\"%d\" where UNAME=\"%s\"", smtp, uname);
				try {
					Statement msg = c.createStatement();
					msg.executeQuery(sql);
					return true;
				} catch (SQLException e) {
					if (!e.getMessage().equals("query does not return ResultSet"))
					throw new ClientRequestException("SQL Exception: " + e.getMessage());
				}
			}
		}
		return false;
	}
	
/*-----------------------------GETTERS-------------------------------------------------*/
	
	public String getServer(String uname) {
		String server = client.getDefServer();
		String sql = String.format("select SERVER from USERS where UNAME=\"%s\"", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("SERVER") != null) server = resp.getString("SERVER");
		} catch (SQLException e) {
			//Nothing needs to be done, will just go with the default.
		}
		return server;
	}
	
	public int getIMAPPort(String uname) {
		int port = client.getDefIMAPPort();
		String sql = String.format("select IMAP from USERS where UNAME=\"%s\"", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("IMAP") != null) port = resp.getInt("IMAP");
		} catch (SQLException e) {
			//Nothing
		}
		return port;
	}
	
	public int getSMTPPort(String uname) {
		int port = client.getDefSMTPPort();
		String sql = String.format("select SMTP from USERS where UNAME=\"%s\"", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("SMTP") != null) port = resp.getInt("SMTP");
		} catch (SQLException e) {
			//Nothing
		}
		return port;
	}
}
