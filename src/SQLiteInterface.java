import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


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
			if (resp.getString("username") != null) return true;
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
			
			if (resp.getString("password") != null) { //This needs to be more secure, but for now let it be
				try {
					//pword
					String pwordhash = Crypt.getPwordHash(pword);
					String storedhash = resp.getString("password");
					if (pwordhash.equals(storedhash)) return true;
				} catch (CryptException e) {
					/* this should never happen */
					System.out.println("CryptException, this shouldn't happen");
					return false;
				}
			}
		} catch (SQLException e) {
			if (e.getMessage().equals("ResultSet closed")) return false;
		}
		return false;
	}
	
	/*-----------------------------SETTERS-------------------------------------------------*/
	
	private void init(Connection dbc) throws SQLException {
		ArrayList<String> createTables = new ArrayList<String>();
		createTables.add("CREATE TABLE 'emails' (	'email_id'	INTEGER NOT NULL,	'email_username'	TEXT NOT NULL,	'email_mailbox'	INTEGER NOT NULL,	'email_date'	TEXT NOT NULL,	'email_to'	TEXT NOT NULL,	'email_from'	TEXT NOT NULL,	'email_subject'	TEXT NOT NULL,	'email_body'	TEXT NOT NULL,	'email_read'	TEXT NOT NULL,	PRIMARY KEY('email_id'),	FOREIGN KEY('email_username') REFERENCES 'user'('username'),	FOREIGN KEY('email_mailbox') REFERENCES 'mailboxes'('mailbox_id'))");
		createTables.add("CREATE TABLE 'mailboxes' (	'mailbox_id'	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,	'username'	TEXT NOT NULL,	'mailbox'	TEXT NOT NULL,	FOREIGN KEY('username') REFERENCES 'users'('username'));");
		createTables.add("CREATE TABLE IF NOT EXISTS `users` (	`username`	TEXT NOT NULL,	`password`	TEXT NOT NULL,	`server`	TEXT NOT NULL DEFAULT '127.0.0.1',	`smtp`	INTEGER NOT NULL DEFAULT 465,	`imap`	INTEGER NOT NULL DEFAULT 993,	`key`	INTEGER NOT NULL DEFAULT 'foobar',	PRIMARY KEY(`username`))");
		
		for (String sql : createTables) {
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
		String pwordhash;
		try {
			pwordhash = Crypt.getPwordHash(pword);
		} catch (CryptException e) {
			throw new ClientRequestException(e.getMessage());
		}
		String sql = String.format("insert into users values(\"%s\", \"%s\", \"%s\", %d, %d, \"%s\")", uname, pwordhash, server, smtp, imap, key);
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
				} catch (SQLException e) {
					if (e.getMessage().equals("query does not return ResultSet")) return true;
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
				} catch (SQLException e) {
					if (e.getMessage().equals("query does not return ResultSet")) return true;
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
				} catch (SQLException e) {
					if (e.getMessage().equals("query does not return ResultSet")) return true;
					throw new ClientRequestException("SQL Exception: " + e.getMessage());
				}
			}
		}
		return false;
	}
	
	public boolean setKey(String uname, String pword, String key) throws ClientRequestException {
		if (this.chkUserExists(uname)) {
			if (this.chkCreds(uname, pword)) {
				String sql = String.format("update users set key=\"%d\" where username=\"%s\"", key, uname);
				try {
					Statement msg = c.createStatement();
					msg.executeQuery(sql);
				} catch (SQLException e) {
					if (e.getMessage().equals("query does not return ResultSet")) return true;
					throw new ClientRequestException("SQL Exception: " + e.getMessage());
				}
			}
		}
		return false;
	}
	
	public boolean createMailbox(String mailbox, String uName) throws ClientRequestException {
		if (mailboxExists(mailbox, uName)) return true;
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
	
	public String getEmailSubject(String id) throws ClientRequestException {
		String subject = null;
		String sql = String.format("select email_subject from emails where email_id='%s'", id);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("email_subject") != null) subject = resp.getString("email_subject");
			if (subject != null) return Crypt.decrypt(subject, client.getAuth().getCreds()[1]);
		} catch (SQLException e) {
			throw new ClientRequestException(e.getMessage());
		} catch (CryptException e) {
			throw new ClientRequestException(e.getMessage());
		}
		throw new ClientRequestException("Retrieval failed");
	}
	
	public String getEmailDate(String id) throws ClientRequestException {
		String subject = null;
		String sql = String.format("select email_date from emails where email_id='%s'", id);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("email_date") != null) subject = resp.getString("email_date");
		} catch (SQLException e) {
			throw new ClientRequestException("SQL Error " + e.getMessage());
		}
		if (subject != null) return subject;
		else throw new ClientRequestException("No date for id " + id);
	}
	
	public String getEmailTo(String id) throws ClientRequestException {
		String subject = null;
		String sql = String.format("select email_to from emails where email_id='%s'", id);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("email_to") != null) subject = resp.getString("email_to");
			if (subject != null) return Crypt.decrypt(subject, client.getAuth().getCreds()[1]);
		} catch (SQLException e) {
			throw new ClientRequestException(e.getMessage());
		} catch (CryptException e) {
			throw new ClientRequestException(e.getMessage());
		}
		throw new ClientRequestException("Retrieval failed");
	}
	
	public String getEmailFrom(String id) throws ClientRequestException {
		String subject = null;
		String sql = String.format("select email_from from emails where email_id='%s'", id);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("email_from") != null) subject = resp.getString("email_from");
			if (subject != null) return Crypt.decrypt(subject, client.getAuth().getCreds()[1]);
		} catch (SQLException e) {
			throw new ClientRequestException(e.getMessage());
		} catch (CryptException e) {
			throw new ClientRequestException(e.getMessage());
		}
		throw new ClientRequestException("Retrieval failed");
	}
	
	public String getEmailBody(String id) throws ClientRequestException {
		String body = null;
		String sql = String.format("select email_body from emails where email_id='%s'", id);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("email_body") != null) body = resp.getString("email_body");
			if (body != null) return Crypt.decrypt(body, client.getAuth().getCreds()[1]).replaceAll("\\n", "\n");
		} catch (SQLException e) {
			throw new ClientRequestException(e.getMessage());
		} catch (CryptException e) {
			throw new ClientRequestException(e.getMessage());
		}
		throw new ClientRequestException("Retrieval failed");
	}
	
	public String getEmailMailbox(String id) throws ClientRequestException {
		String body = null;
		String sql = String.format("select mailbox from emails inner join mailboxes on emails.email_mailbox=mailboxes.mailbox_id where email_id='%s'", id);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("mailbox") != null) body = resp.getString("mailbox");
			if (body != null) return body;
		} catch (SQLException e) {
			throw new ClientRequestException(e.getMessage());
		}
		throw new ClientRequestException("Retrieval failed");
	}
	
	
	public String getServer(String uname) {
		String server = client.getDefServer();
		String sql = String.format("select server from users where username='%s'", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("server") != null) server = resp.getString("server");
		} catch (SQLException e) {
			//Nothing needs to be done, will just go with the default.
		}
		return server;
	}
	
	public int getIMAPPort(String uname) {
		int port = client.getDefIMAPPort();
		String sql = String.format("select imap from users where username='%s'", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("imap") != null) port = resp.getInt("imap");
		} catch (SQLException e) {
			//Nothing
		}
		return port;
	}
	
	public int getSMTPPort(String uname) {
		int port = client.getDefSMTPPort();
		String sql = String.format("select smtp from users where username='%s'", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("smtp") != null) port = resp.getInt("smtp");
		} catch (SQLException e) {
			//Nothing
		}
		return port;
	}
	
	public String getUserKey(String uname) {
		String userKey = client.getDefaultUserKey();
		String sql = String.format("select key from users where username='%s'", uname);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.next() && resp.getString("key") != null) userKey = resp.getString("key");
		} catch (SQLException e) {
			//Nothing
		}
		return userKey;
	}
	
	public ArrayList<String> getEmailIds(String uname) {
		ArrayList<String> emailIds = new ArrayList<String>();
		String sql = String.format("select email_id from emails inner join users on emails.email_username=users.username where username='%s' order by email_date", uname);
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
	
	public ArrayList<String> getEmailsByMailbox(String uname, String mailbox) {
		ArrayList<String> emailIds;
		String sql = String.format("select email_id from emails inner join users on emails.email_username=users.username inner join mailboxes on emails.email_mailbox=mailboxes.mailbox_id where username='%s' and mailbox='%s' order by email_date", uname, mailbox);
		return getEmailsByMailboxDefined(sql);
	}
	
	public ArrayList<String> getEmailsByMailbox(String uname, String mailbox, boolean read) {
		ArrayList<String> emailIds;
		String readString;
		if (read) readString = "t";
		else readString = "f";
		String sql = String.format("select email_id from emails inner join users on emails.email_username=users.username inner join mailboxes on emails.email_mailbox=mailboxes.mailbox_id where username='%s' and mailbox='%s' and email_read='%s' order by email_date", uname, mailbox, readString);
		return getEmailsByMailboxDefined(sql);
	}

	private ArrayList<String> getEmailsByMailboxDefined(String sql) {
		ArrayList<String> emailIds = new ArrayList<String>();
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
	
	public ArrayList<String> getReadEmailIds(String uname) {
		ArrayList<String> emailIds = new ArrayList<String>();
		String sql = String.format("select email_id from emails inner join users on emails.email_username=users.username where username='%s' and email_read='t' order by email_date", uname);
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
	
	public ArrayList<String> getUnreadEmailIds(String uname) {
		ArrayList<String> emailIds = new ArrayList<String>();
		String sql = String.format("select email_id from emails inner join users on emails.email_username=users.username where username='%s' and email_read='f' order by email_date", uname);
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
			while (resp.next() && (emailId = resp.getString("mailbox_id")) != null)
				mailboxes.add(emailId);
		} catch (SQLException e) {
			//Nothing
		}
		if (mailboxes.size() > 0) return true;
		else return false;
	}

	public ArrayList<String> getAllMailboxNames(String uName) throws ClientRequestException {
		ArrayList<String> mailboxes = new ArrayList<String>();
		String sql = String.format("select mailbox from mailboxes where username='%s'", uName);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			String mailbox;
			while (resp.next() && (mailbox = resp.getString("mailbox")) != null)
				mailboxes.add(mailbox);
		} catch (SQLException e) {
			//Nothing
		}
		return mailboxes;
	}
	

	public String getMailboxID(String mailbox, String uName) {
		//I know this is a copy/paste job, fix this later
		ArrayList<String> mailboxes = new ArrayList<String>();
		String sql = String.format("select mailbox_id from mailboxes where mailbox='%s' and username='%s'", mailbox, uName);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			String emailId;
			while (resp.next() && (emailId = resp.getString("mailbox_id")) != null)
				mailboxes.add(emailId);
		} catch (SQLException e) {
			//Nothing
		}
		if (mailboxes.size() == 1) return mailboxes.get(0);
		else return null;
	}


	public boolean addEmail(HashMap<String, String> emailData) throws ClientRequestException {
		boolean valid = false;
		//These are okay, they will be prefixed with email_ a few lines down
		ArrayList<String> validFields = new ArrayList<String>(Arrays.asList(new String[] {"email_id", "mailbox", "date", "to", "from", "subject", "body", "read"}));
		//the old boolean backflip, sorry not sorry
		if (emailData.keySet().size() == validFields.size()) {
			valid = true;
			for (String key : emailData.keySet()) {
				if (!validFields.contains(key)) valid = false;
			}
		}
		try {
			if (valid) {
				//Encrypt sensitive fields "to", "from", "subject", "body"
				String[] sensitiveFields = new String[] {"to", "from", "subject", "body"};
				try {
					for (String field : sensitiveFields) {
						String value = emailData.get(field);
						emailData.put(field, Crypt.encrypt(value, client.getAuth().getCreds()[1]));
					}
				} catch (CryptException e) {
					throw new ClientRequestException(e.getMessage());
				}

				emailData.put("username", client.getUname());
				ArrayList<String> fields = new ArrayList<String>();
				ArrayList<String> values = new ArrayList<String>();
				for (String key : emailData.keySet()) {
					if (!key.equals("email_id"))
						fields.add("email_" + key);
					else
						fields.add(key);
					if (!key.equals("email_id") && !key.equals("email_mailbox"))
						values.add("'" + emailData.get(key) + "'");
					else
						values.add(emailData.get(key));
				}
				String fieldsString = String.join(", ", fields);
				String valuesString = String.join(", ", values);
				String sql = String.format("insert into emails (%s) values (%s)", fieldsString, valuesString);
				try {
					Statement msg = c.createStatement();
					msg.executeQuery(sql);
				} catch (SQLException e) {
					if (e.getMessage().equals("query does not return ResultSet")) return true;
					throw new ClientRequestException(e.getMessage());
				}
			}
		} catch (ClientRequestException e) {
			throw e;
		}
			return false;
		}
}
