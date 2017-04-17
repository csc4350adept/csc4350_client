import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class AdeptConnection {
	private Client client;
	private Authenticate.proto p;
	
	private SSLSocket s;
	private InputStream input;
	private OutputStream output;
	
	public AdeptConnection(Client client, String host, int port, Authenticate.proto p) throws ClientRequestException {
		this.client = client;
		this.p = p;
		KeyManager[] keyManagers;
		try {
			try {
				//Get keystore
				KeyStore keys = KeyStore.getInstance("JKS");
				File keysFile = new File(client.getKeysFilePath());
				if(!keysFile.exists() || keysFile.isDirectory()) throw new Error("Key file does not exist or is directory"); //Create it here when we figure out how
				keys.load(new FileInputStream(client.getKeysFilePath()), client.getKeysFilePwd());
				KeyManagerFactory keyFactory = KeyManagerFactory.getInstance("SunX509");
				keyFactory.init(keys, client.getKeysPwd());
				keyManagers = keyFactory.getKeyManagers();
			} catch (Exception e) {
				throw new Error("Error initializing connection. KeyStore generation failed.");
			}
					
			//Get trustmanager -- trustmanager should only trust provided key from 
			
			SSLContext context = SSLContext.getInstance("TLS");
			//Needs keystore and trustmanager
			context.init(keyManagers, null, null);
			SSLSocketFactory ssf = context.getSocketFactory();
			
			s = (SSLSocket) ssf.createSocket(host, port);
			
			input = s.getInputStream();
			output = s.getOutputStream();
			
		} catch (java.net.ConnectException e) {
			throw new ClientRequestException("Connection failed. " + e.getMessage());
		} catch (Exception e) {
			//what
			System.out.println("Everything is fine, I swear " + e.getMessage());
			e.printStackTrace();;
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
			if (output == null) System.out.println("output is null!");
			output.write(msg);
			//System.out.println(input.read());
		} catch (IOException e) {
			throw new ClientRequestException("IOException in sendMsg().");
		}
		return "";
	}

}