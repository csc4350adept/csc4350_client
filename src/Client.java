import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;
import java.util.ArrayList;

public class Client {
	private String defaultServer = "localhost";
	private int defaultIMAPPort = 993;
	private int defaultSMTPPort = 465;
	private String defaultUserKey = "ABC";
	
	private String keysFilePath;
	private char[] keysFilePwd;
	private char[] keysPwd;
	
	private Authenticate authenticate;
	private SQLiteInterface db;
	
	public Client() {
		//Need some error catching here
		authenticate = new Authenticate(this);
		db = new SQLiteInterface(this);
		
		//Create keyStore
		keysFilePath = "KeyStoreC.jks";
		keysFilePwd = "foobar".toCharArray();
		keysPwd = "foobar".toCharArray();
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
	
	public String getDefaultUserKey() {
		return defaultUserKey;
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
	
	public String getUserKey(String uname) {
		return db.getUserKey(uname);
	}
	
	public String getKeysFilePath() {
		return keysFilePath;
	}
	
	public char[] getKeysFilePwd() {
		return keysFilePwd;
	}
	
	public char[] getKeysPwd() {
		return keysPwd;
	}
	
	
	/*-----------------------------User Functions-------------------------------------------------*/
	
	public String getEmailSubject(String id) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		if (!id.matches("[0-9]+")) throw new ClientRequestException("ID is not a number");
		try {
			return db.getEmailSubject(id);
		} catch (ClientRequestException e) {
			throw e;
		}
	}
	
	public boolean setServer(String uname, String server) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		String[] creds = authenticate.getCreds();
		try {
			return db.setServer(creds[0], creds[1], server);
		} catch (ClientRequestException e) {
			throw e;
		}
	}
	
	public boolean setSMTP(String uname, String smtp) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		String[] creds =authenticate.getCreds();
		try {
			return db.setSMTP(creds[0], creds[1], Integer.parseInt(smtp));
		} catch (ClientRequestException e) {
			throw e;
		} catch (NumberFormatException e) {
			throw new ClientRequestException("SMTP Port must be a number");
		}
	}
	
	public boolean setIMAP(String uname, String imap) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		String[] creds =authenticate.getCreds();
		try {
			return db.setSMTP(creds[0], creds[1], Integer.parseInt(imap));
		} catch (ClientRequestException e) {
			throw e;
		} catch (NumberFormatException e) {
			throw new ClientRequestException("SMTP Port must be a number");
		}
	}
	
	public boolean setKey(String uname, String key) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		String[] creds = authenticate.getCreds();
		try {
			return db.setKey(creds[0], creds[1], key);
		} catch (ClientRequestException e) {
			throw e;
		}
	}
	
	public boolean chkUserExists(String uname) {
		return db.chkUserExists(uname);
	}
	
	public boolean mkUser(String uname, String pword, String server, int imap, int smtp) throws ClientRequestException {
		if (!db.chkUserExists(uname)) {
			authenticate.setCreds(uname, pword);
			if (authenticate.chkCreds(Authenticate.proto.IMAP, server, imap)) {
				return db.mkCreds(uname, pword, server, imap, smtp, defaultUserKey);
			}
		}
		return false;
	}
	
	
	public boolean authenticate(String uname, String pword) throws ClientRequestException {
		authenticate.setCreds(uname,  pword);
		try {
			if (authenticate.chkCreds()) {
				return true;
			}
		} catch (ClientRequestException e) {
			throw e;
		}
		return false;
	}
	
	public void clearAuthentication() {
		authenticate.clearCreds();
	}
	
	public String getUname() throws ClientRequestException {
		if (authenticate.isAuthenticated()) return authenticate.getUname();
		throw new ClientRequestException("Client is not authenticated");
	}
	
	public boolean update() throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		AdeptConnection c = new AdeptConnection(this, db.getServer(authenticate.getUname()), db.getIMAPPort(authenticate.getUname()), Authenticate.proto.IMAP);
		try {
			return Update.requestUpdate(this, c);
		} catch (ClientRequestException e) {
			throw e;
		}
	}	
	
	public ArrayList<String> getAllEmailIds(String uname) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		return db.getEmailIds(uname);
	}
	
	public ArrayList<String> getReadEmailIds(String uname) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		return db.getReadEmailIds(uname);
	}
	
	public ArrayList<String> getUnreadEmailIds(String uname) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		return db.getUnreadEmailIds(uname);
	}
	
	public ArrayList<String> getEmailsByMailbox(String uname, String mailbox) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		return db.getEmailsByMailbox(uname, mailbox);
	}
	
	public ArrayList<String> getEmailsByMailbox(String uname, String mailbox, boolean read) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		return db.getEmailsByMailbox(uname, mailbox, read);
	}

	public String getEmailDate(String id) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		try {
			return db.getEmailDate(id);
		} catch (ClientRequestException e) {
			throw e;
		}
	}

	public String getEmailTo(String id) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		try {
			return db.getEmailTo(id);
		} catch (ClientRequestException e) {
			throw e;
		}
	}

	public String getEmailFrom(String id) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		try {
			return db.getEmailFrom(id);
		} catch (ClientRequestException e) {
			throw e;
		}
	}

	public String getEmailBody(String id) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		try {
			return db.getEmailBody(id);
		} catch (ClientRequestException e) {
			throw e;
		}
	}

	public boolean setEmailRead(String id) throws ClientRequestException {
		if (!authenticate.isAuthenticated()) throw new ClientRequestException("Not authenticated");
		try {
			return EditEmail.setEmailRead(this, id);
		} catch (ClientRequestException e) {
			throw e;
		}
	}
}
