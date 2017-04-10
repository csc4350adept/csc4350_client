
public class Authenticate {
	Client client;
	private String uname;
	private String pword;
	private String resp;
	
	public Authenticate(Client client) {
		this.client = client;
		uname = null;
		pword = null;
	}
	
	public static enum proto{
	    IMAP, SMTP 
	}
	
	public void setCreds(String uname, String pword) {
		this.uname = uname;
		this.pword = pword;
	}
	
	public boolean chkCreds() throws ClientRequestException {
		if (uname != null && pword != null) {
			System.out.println("getting local auth");
			boolean localAuth = client.getDB().chkCreds(uname, pword);
			if (!localAuth && client.getDB().chkUserExists(uname)) return false;
			boolean remoteAuth = client.authenticate(Authenticate.proto.IMAP, uname, pword);
			if (!localAuth && remoteAuth) {
				boolean acctCreation = client.getDB().mkCreds(uname, pword);
				if (acctCreation) return true;
			} else if (localAuth && remoteAuth) return true;
			return false;
		}
		throw new ClientRequestException("Credentials not set.");
	}

	public boolean chkCreds(proto p) throws ClientRequestException {
		if (uname == null || pword == null) throw new ClientRequestException("Credentials not set.");
		if (p == proto.IMAP) {
			System.out.println("IMAP!!!");
			//IMAP authentication
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
