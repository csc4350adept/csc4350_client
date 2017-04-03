
public class Authenticate {
	private String uname;
	private String pword;
	private String resp;
	
	public Authenticate(String uname, String pword) {
		this.uname = uname;
		this.pword = pword;
	}
	
	public enum proto{
	    IMAP, SMTP 
	}

	public boolean chkCreds(proto p) {
		if (p == proto.IMAP) {
			System.out.println("IMAP!!!");
			//TODO create a connection, and send the command
		} else if (p == proto.SMTP) {
			System.out.println("SMTP!!!!");
		}
		return false;
	}
	
	public String getResp() {
		return resp;
	}
}
