import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;

public class Client {
	private String defaultServer = "localhost";
	private int defaultIMAPPort = 993;
	private int defaultSMTPPort = 465;
	
	private Authenticate authenticate;
	private SQLiteInterface db;
	
	public Client() {
		//Need some error catching here
		authenticate = new Authenticate(this);
		db = new SQLiteInterface(this);
	}
	
	/*-----------------------------Getters-------------------------------------------------*/
	
	public SQLiteInterface getDB() {
		return db;
	}
	
	public Authenticate getAuth() {
		return authenticate;
	}
	
	public String getDefServer() {
		return defaultServer;
	}
	
	public int getDefIMAPPort() {
		return defaultIMAPPort;
	}
	
	public int getDefSMTPPort() {
		return defaultSMTPPort;
	}
	
	public String getServer(String uname) {
		return db.getServer(uname);
	}
	
	public int getIMAP(String uname) {
		return db.getIMAPPort(uname);
	}
	
	public int getSMTP(String uname) {
		return db.getSMTPPort(uname);
	}
	
	/*-----------------------------User Functions-------------------------------------------------*/
	
	public boolean chkUserExists(String uname) {
		return db.chkUserExists(uname);
	}
	
	public boolean mkUser(String uname, String pword, String server, int imap, int smtp) throws ClientRequestException {
		if (!db.chkUserExists(uname)) {
			authenticate.setCreds(uname, pword);
			if (authenticate.chkCreds(Authenticate.proto.IMAP, server, imap)) {
				return db.mkCreds(uname, pword, server, imap, smtp);
			}
		}
		return false;
	}
	
	
	public boolean authenticate(String uname, String pword) throws ClientRequestException {
		authenticate.setCreds(uname,  pword);
		try {
			if (authenticate.chkCreds()) {
				if (authenticate.chkCreds(Authenticate.proto.IMAP)) {
					return true;
				}
			}
		} catch (ClientRequestException e) {
			throw e;
		}
		return false;
	}
	
}
