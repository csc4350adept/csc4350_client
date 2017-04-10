import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;

public class Client {
	private Authenticate authenticate;
	private SQLiteInterface db;
	
	public Client() {
		//Need some error catching here
		authenticate = new Authenticate(this);
		db = new SQLiteInterface(this);
	}
	
	public SQLiteInterface getDB() {
		return db;
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
	
	public boolean authenticate(Authenticate.proto proto, String uname, String pword) throws ClientRequestException {
		authenticate.setCreds(uname, pword);
		return true;
		/*
		Authenticate a = new Authenticate(uname, pword);
		this.isAuthenticated = a.chkCreds(proto);
		if (!this.isAuthenticated) throw new ClientRequestException(a.getResp());
		else return true;
		*/
	}
	
	
}
