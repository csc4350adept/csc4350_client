import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Update {

	public static boolean requestUpdate(Client client, AdeptConnection c) throws ClientRequestException {
		String uName = client.getUname();
		String msg;
		String resp;
		SQLiteInterface db = client.getDB();
		
		try {
			msg = String.format("LIST %s", uName);
			resp = c.request(msg);
			HashMap<String, String> emailsMailboxes = new HashMap<String, String>();
			
			//Get mailboxes
			if (resp.startsWith("OK - List Completed") && resp.split("\n").length > 1) {
				ArrayList<String> mailboxes = new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(resp.split("\n"), 1, resp.split("\n").length)));
				for (String mailbox : mailboxes) {
					//Update local db with new mailboxes
					if (!db.mailboxExists(mailbox, uName)) db.createMailbox(mailbox, uName);
					//Get email_ids
					msg = String.format("LIST %s %s", uName, mailbox);
					resp = c.request(msg);
					if (resp.startsWith("OK - List Completed") && resp.split("\n").length > 1) {
						ArrayList<String> email_ids = new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(resp.split("\n"), 1, resp.split("\n").length)));
						for (String email_id : email_ids)
							emailsMailboxes.put(email_id, db.getMailboxID(mailbox, uName));
					}
				}
			}
			
			
			//Get list of local emails
			ArrayList<String> localEmails = db.getEmailIds(uName);
			
			
			
			for (String email_id : emailsMailboxes.keySet()) {
				String mailbox = emailsMailboxes.get(email_id);
				//If we don't have this email locally, fetch it and store it
				if (!localEmails.contains(email_id)) {
					msg = String.format("FETCH %s HEADER", email_id);
					resp = c.request(msg);
					if (resp.startsWith("OK - List Completed") && resp.split("\n").length > 1) {
						//Parse out each line
						ArrayList<String> headers = new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(resp.split("\n"), 1, resp.split("\n").length)));
						System.out.println("\n\n" + String.join("\n", headers) + "\n\n");
					}
				}
			}
			
			
		} catch (ClientRequestException e) {
			throw e;
		}
		return false;
	}
	
	
}
