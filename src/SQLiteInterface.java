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
import java.util.ArrayList;


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
			System.out.println("Shit");
			e.printStackTrace();
		}
		File dir = new File(String.join("/", new String[]{decodedPath, defaultSQLitePath.split("/")[0]}));
		dir.mkdir();
		File f = new File(decodedPath + defaultSQLitePath);
		String dbPath = "jdbc:sqlite:" + decodedPath + defaultSQLitePath;
		if(dbPath.startsWith("/")) {
			dbPath = dbPath.substring(1);
		}
		Connection dbConn = null;
		if (!f.exists()) {
			//Create SQLite database
			try {
				dbConn = DriverManager.getConnection(dbPath);
				DatabaseMetaData meta = dbConn.getMetaData();
				this.init(dbConn);
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
		String sql = String.format("select username from users where username = \"%s\"", uname);
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
		String sql = String.format("select password from users where username = \"%s\"", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("password") != null && pword.equals(resp.getString("password"))) { //This needs to be more secure, but for now let it be
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
		ArrayList<String> createTables = new ArrayList<String>();
		createTables.add("CREATE TABLE 'emails' (	'email_id'	INTEGER NOT NULL,	'username'	TEXT NOT NULL,	'mailbox'	INTEGER NOT NULL,	'to'	TEXT NOT NULL,	'from'	TEXT NOT NULL,	'subject'	TEXT NOT NULL,	'body'	TEXT NOT NULL,	'read'	TEXT NOT NULL,	PRIMARY KEY('email_id'),	FOREIGN KEY('username') REFERENCES 'user'('username'),	FOREIGN KEY('mailbox') REFERENCES 'mailboxes'('mailbox_id'))");
		createTables.add("CREATE TABLE 'mailboxes' (	'mailbox_id'	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,	'username'	TEXT NOT NULL,	'mailbox'	TEXT NOT NULL,	FOREIGN KEY('username') REFERENCES 'users'('username'));");
		createTables.add("CREATE TABLE IF NOT EXISTS `users` (	`username`	TEXT NOT NULL,	`password`	TEXT NOT NULL,	`server`	TEXT NOT NULL DEFAULT '127.0.0.1',	`smtp`	INTEGER NOT NULL DEFAULT 465,	`imap`	INTEGER NOT NULL DEFAULT 993,	`key`	INTEGER NOT NULL DEFAULT 'foobar',	PRIMARY KEY(`username`))");
		
		for (String sql : createTables) {
			System.out.println(sql);
			try {
				Statement msg = dbc.createStatement();
				msg.executeQuery(sql);
			} catch (SQLException e) {
				if (!e.getMessage().equals("query does not return ResultSet")) throw e;
			}
		}
	}
	
	public boolean mkCreds(String uname, String pword) throws ClientRequestException {
		String server = client.getDefServer();
		int imap = client.getDefIMAPPort();
		int smtp = client.getDefSMTPPort();
		String key = client.getDefaultUserKey();
		return this.mkCreds(uname, pword, server, imap, smtp, key);
	}
	
	public boolean mkCreds(String uname, String pword, String server, int imap, int smtp, String key) throws ClientRequestException {
		String sql = String.format("insert into users values(\"%s\", \"%s\", \"%s\", %d, %d, \"%s\")", uname, pword, server, smtp, imap, key);
		Statement msg;
		try {
			msg = c.createStatement();
			msg.executeQuery(sql);
		} catch (SQLException e) {
			if (e.getMessage().equals("query does not return ResultSet")) return true;
			System.out.println(e.getMessage());
			throw new ClientRequestException(e.getMessage());
		}
		return false;
	}
	
	public boolean setServer(String uname, String pword, String server) throws ClientRequestException {
		if (this.chkUserExists(uname)) {
			if (this.chkCreds(uname, pword)) {
				String sql = String.format("update users set server=\"%s\" where username=\"%s\"", server, uname);
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
				String sql = String.format("update users set imap=\"%d\" where username=\"%s\"", imap, uname);
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
				String sql = String.format("update users set smtp=\"%d\" where username=\"%s\"", smtp, uname);
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
	
	public boolean createMailbox(String mailbox, String uName) throws ClientRequestException {
		String sql = String.format("insert into mailboxes (username, mailbox) values('%s', '%s')", uName, mailbox);
		Statement msg;
		try {
			msg = c.createStatement();
			msg.executeQuery(sql);
		} catch (SQLException e) {
			if (!e.getMessage().equals("query does not return ResultSet")) throw new ClientRequestException("SQL Exception: " + e.getMessage());
		}
		return true;
	}

	
/*-----------------------------GETTERS-------------------------------------------------*/
	
	public String getServer(String uname) {
		String server = client.getDefServer();
		String sql = String.format("select server from users where username=\"%s\"", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("server") != null) server = resp.getString("server");
		} catch (SQLException e) {
			//Nothing needs to be done, will just go with the default.
		}
		return server;
	}
	
	public int getIMAPPort(String uname) {
		int port = client.getDefIMAPPort();
		String sql = String.format("select imap from users where username=\"%s\"", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("imap") != null) port = resp.getInt("imap");
		} catch (SQLException e) {
			//Nothing
		}
		return port;
	}
	
	public int getSMTPPort(String uname) {
		int port = client.getDefSMTPPort();
		String sql = String.format("select smtp from users where username=\"%s\"", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("smtp") != null) port = resp.getInt("smtp");
		} catch (SQLException e) {
			//Nothing
		}
		return port;
	}
	
	public ArrayList<String> getEmailIds(String uname) {
		ArrayList<String> emailIds = new ArrayList<String>();
		String sql = String.format("select email_id from emails inner join users on emails.username=users.username where username='%s'", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			String emailId;
			while (resp.next() && (emailId = resp.getString("email_id")) != null)
				emailIds.add(emailId);
		} catch (SQLException e) {
			//Nothing
		}
		return emailIds;
	}


	public boolean mailboxExists(String mailbox, String uName) {
		ArrayList<String> mailboxes = new ArrayList<String>();
		String sql = String.format("select mailbox_id from mailboxes where mailbox='%s' and username='%s'", mailbox, uName);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			String emailId;
			while (resp.next() && (emailId = resp.getString("email_id")) != null)
				mailboxes.add(emailId);
		} catch (SQLException e) {
			//Nothing
		}
		if (mailboxes.size() == 1) return true;
		else return false;
	}


	

	public String getMailboxID(String mailbox, String uName) {
		//I know this is a copy/paste job, fix this later
		ArrayList<String> mailboxes = new ArrayList<String>();
		String sql = String.format("select mailbox_id from mailboxes where mailbox='%s' and username='%s'", mailbox, uName);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			String emailId;
			while (resp.next() && (emailId = resp.getString("email_id")) != null)
				mailboxes.add(emailId);
		} catch (SQLException e) {
			//Nothing
		}
		if (mailboxes.size() == 1) return mailboxes.get(0);
		else return null;
	}
}
