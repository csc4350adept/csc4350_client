
public class Authenticate {
	Client client;
	private String uname;
	private String pword;
	private String resp;
	public boolean isAuthenticated = false;
	
	public Authenticate(Client client) {
		this.client = client;
		uname = null;
		pword = null;
	}
	
	public static enum proto{
	    IMAP, SMTP 
	}
	
	public boolean isAuthenticated() {
		return isAuthenticated;
	}
	
	public void setCreds(String uname, String pword) {
		this.uname = uname;
		this.pword = pword;
		this.isAuthenticated = false;
		System.out.println("SETTING CREDENTIALS");
	}
	
	public String getUname() {
		return uname;
	}
	
	public String[] getCreds() {
		return new String[] {uname, pword};
	}
	
	public boolean chkCreds() throws ClientRequestException {
		if (uname != null && pword != null) {
			//If we already did this...
			if (isAuthenticated) return true;
			
			//Get local authentication
			System.out.println("getting local auth");
			boolean localAuth = client.getDB().chkCreds(uname, pword);
			System.out.println(localAuth);
			
			
			//If local authentication succeeds, set isAuthenticated and return true
			if (localAuth) {
				isAuthenticated = true;
				return true;
			}
			
			//If local authentication failed, check if the user exists
			boolean localUserExists = client.getDB().chkUserExists(uname);
			System.out.println("User exists: " + localUserExists);
			//If the user does exist, return false.
			if (!localAuth && localUserExists) return false;
			//If the user does not exist, do a remote auth and create on upon success
			if (!localAuth && !localUserExists) {
				System.out.println("Need to create user");
				boolean remoteAuth = this.chkCreds(Authenticate.proto.IMAP);
				if (remoteAuth) {
					System.out.println("remote auth succeeded");
					boolean acctCreation = client.getDB().mkCreds(uname, pword);
					System.out.println("acctCreation " + acctCreation);
					if (acctCreation) {
						isAuthenticated = true;
						return true;
					}
				}
				System.out.println("remote auth failed");
			}
			
			//Return false if nothing worked
			return false;
		}
		throw new ClientRequestException("Credentials not set.");
	}
	
	public boolean chkCreds(proto p) throws ClientRequestException {
		if (uname == null || pword == null) throw new ClientRequestException("Credentials not set.");
		String server = client.getServer(uname);
		int port;
		if (p == proto.IMAP) port = client.getIMAP(uname);
		else port = client.getSMTP(uname);
		try {
			return this.chkCreds(p, server, port);
		} catch (ClientRequestException e) {
			throw e;
		}
	}

	public boolean chkCreds(proto p, String server, int port) throws ClientRequestException {
		if (uname == null || pword == null) throw new ClientRequestException("Credentials not set.");
		if (p == proto.IMAP) {
			System.out.println("IMAP!!!");
			AdeptConnection adept = new AdeptConnection(client, server, port, p);
			try {
				return adept.authenticate();
			} catch (ClientRequestException e) {
				throw e;
			}
		} else if (p == proto.SMTP) {
			//SMTP authentication
			System.out.println("SMTP!!!!");
		}
		return false;
	}
	
	public String getResp() {
		return resp;
	}
}
