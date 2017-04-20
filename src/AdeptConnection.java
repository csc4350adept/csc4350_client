import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
			//Create TrustManager
			//Server doesn't verify identity of clients at initial connection
			//Only clients verify the identity of the server
			TrustManager trustManager = new X509TrustManager() {

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					//Be naive
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					//Be naive
					
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					//Be naive
					return null;
				}
			};	
			
			
			SSLContext context = SSLContext.getInstance("TLS");
			//Needs keystore and trustmanager
			context.init(keyManagers, new TrustManager[] {trustManager}, null);
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
	
	public String request(String msg) throws ClientRequestException {
		String resp = null;
		try {
			System.out.println("Authenticating");
			if(authenticate())
				resp = sendMsg(msg);
			if (resp != null)
				s.close();
				return resp;
		} catch (ClientRequestException e) {
			throw e;
		} catch (IOException e) {
			//If the server closes the socket before we do, we still want the response
			return resp;
		}
	}
	
	public boolean authenticate() throws ClientRequestException {
		String uname = this.client.getAuth().getCreds()[0];
		String pword = this.client.getAuth().getCreds()[1];
		try{
			if (p == Authenticate.proto.IMAP) {
				String authString = String.format("LOGIN %s %s", uname, pword);
				String resp = sendMsg(authString.getBytes());
				switch (resp) {
				case "OK - User Authenticated":
					return true;
				case "NO - Login failure: Invalid username or password":
					throw new ClientRequestException("Invalid username or password");
				case "BAD - Invalid or unknown command":
					throw new ClientRequestException("Invalid or unknown command");
				default:
					return false;
				}
			} else if (p == Authenticate.proto.SMTP) {
				/* No SMTP authentication, we only use IMAP on both ports */
				throw new ClientRequestException("Authentication error: SMTP authentication not supported.");
			} else {
				throw new ClientRequestException("Authentication error: Invalid authentication type.");
			}
		} catch (ClientRequestException e) {
			throw e;
		}
	}
	
	private String sendMsg(String msg) throws ClientRequestException {
		return sendMsg(msg.getBytes());
	}
	
	private String sendMsg(byte[] msg) throws ClientRequestException {
		try {
			if (output == null) System.out.println("output is null!");
			output.write(msg);
			System.out.println(String.format("Sent: %s", new String(msg)));
			char inChar;
			String resp = "";
			while((inChar = (char) input.read()) != -1) {
				if (inChar == 0) break;
				resp = resp + inChar;
			}
			System.out.println(String.format("Received: %s", resp));
			return resp;
		} catch (IOException e) {
			throw new ClientRequestException("IOException in sendMsg().");
		}
	}

}