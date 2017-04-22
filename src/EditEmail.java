
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

}
