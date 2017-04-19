public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client c = new Client();
		
		try {
			//c.authenticate(Authenticate.proto.IMAP, "ebull", "foobar");
			c.authenticate("ebull@adept.com", "foobar");
		} catch (ClientRequestException e) {
			System.out.println(e.getMessage());
		}
	}

}
