import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class AdeptConnection {
	private Client client;
	private Authenticate.proto p;
	
	private SSLSocket s;
	private InputStream input;
	private OutputStream output;
	
	public AdeptConnection(Client client, String host, int port, Authenticate.proto p) {
		this.client = client;
		this.p = p;
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, null, null);
			SSLSocketFactory ssf = context.getSocketFactory();
			
			s = (SSLSocket) ssf.createSocket(host, port);
			
			input = s.getInputStream();
			output = s.getOutputStream();
			
		} catch (Exception e) {
			//what
			System.out.println("Everything is fine, I swear " + e.getMessage());
			System.out.println(host + " " + port);
		}
	}
	
	public boolean connect() throws ClientRequestException {
		try {
			System.out.println("Authenticating");
			authenticate();
			//Do stuff here
		} catch (ClientRequestException e) {
			throw e;
		}
		return true;
	}
	
	private void authenticate() throws ClientRequestException {
		String uname = this.client.getAuth().getCreds()[0];
		String pword = this.client.getAuth().getCreds()[1];
		try{
			if (p == Authenticate.proto.IMAP) {
				String authString = String.format("login %s %s", uname, pword);
				String resp = sendMsg(authString.getBytes());
			} else if (p == Authenticate.proto.SMTP) {
				//stuff
			} else {
				throw new ClientRequestException("Authentication error: Invalid authentication type.");
			}
		} catch (ClientRequestException e) {
			throw e;
		}
	}
	
	private String sendMsg(byte[] msg) throws ClientRequestException {
		try {
			output.write(msg);
			//System.out.println(input.read());
		} catch (IOException e) {
			throw new ClientRequestException("IOException in sendMsg().");
		}
		return "";
	}

}