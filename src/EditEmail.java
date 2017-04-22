import java.util.ArrayList;
import java.util.HashMap;

public class EditEmail {
	
	public static boolean setEmailRead(Client client, String id) throws ClientRequestException {
		String resp;
		AdeptConnection c = null;
		try {
			c = new AdeptConnection(client, client.getServer(client.getAuth().getUname()), client.getIMAP(client.getAuth().getUname()), Authenticate.proto.IMAP);
			String msg = String.format("APPEND %s \\READ", id);
			resp = c.request(msg);
			c.close();
		} catch (ClientRequestException e) {
			if (c != null) c.close();
			throw e;
		}
		if (!resp.startsWith("OK")) {
			if (resp.split(" - ").length > 1)
				throw new ClientRequestException(resp.split(" - ")[1]);
			else throw new ClientRequestException("Bad response");
		}
		return true;
	}


	public static boolean sendEmail(Client client, String date, String to, String from, String subject, String body) throws ClientRequestException {
		String resp;
		String msg;
		AdeptConnection c = null;
		try {
			c = new AdeptConnection(client, client.getServer(client.getAuth().getUname()), client.getIMAP(client.getAuth().getUname()), Authenticate.proto.IMAP);
			
			msg = String.format("MAIL FROM: %s", client.getAuth().getUname());
			resp = c.request(msg);
			if (!resp.equals("250 OK")) throw new ClientRequestException(resp);
			
			//Loop if multiple recipients
			for (String rcpt : to.split("(\\s|\\s,|,|,\\s|\\s,\\s)")) {
				msg = String.format("RCPT TO: %s", rcpt);
				resp = c.request(msg);
				if (!resp.equals("250 OK")) throw new ClientRequestException(resp);
			}

			msg = "DATA";
			resp = c.request(msg);
			if (!resp.equals("354 Send message content; end with <CRLF>.<CRLF>")) throw new ClientRequestException(resp);
			
			ArrayList<String> emailData = new ArrayList<String>();
			emailData.add("Date: " + date);
			emailData.add("From: " + from);
			emailData.add("Subject: " + subject);
			emailData.add("To: " + to);
			emailData.add("\r\n");
			emailData.add(body);
			emailData.add("\r\n.\r\n");
			
			msg = String.join("\r\n", emailData);
			resp = c.request(msg);
			if (!resp.equals("250 OK")) throw new ClientRequestException(resp);
			c.close();
		} catch (ClientRequestException e) {
			if (c != null) c.close();
			throw e;
		}
		return true;
	}

}
