public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client c = new Client();
		
		try {
			//c.authenticate(Authenticate.proto.IMAP, "ebull", "foobar");
			String testName = "ebull@adept.com";
			String testPwd = "foobar";
			if (c.authenticate(testName, testPwd)) {
				System.out.println("Authenticated as " + c.getUname());
			} else {
				System.out.println("Authenticated failed");
				return;
			}
		} catch (ClientRequestException e) {
			System.out.println(e.getMessage());
		}
	}

}
