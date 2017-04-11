import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class AdeptConnection {
	
	public AdeptConnection(String host, int port) {
		try {
			SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sslSocket = (SSLSocket) ssf.createSocket(host, port);
			
			InputStream input = sslSocket.getInputStream();
			OutputStream output = sslSocket.getOutputStream();
			
		} catch (Exception e) {
			//what
			System.out.println("Everything is fine, I swear " + e.getMessage());
		}
	}

}