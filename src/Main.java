public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client c = new Client();
		boolean verbose = false;
		boolean cli = false;
		
		if (args.length > 0) {
			for (String arg : args) {
				switch (arg) {
				case "-v":
					verbose = true;
				case "-cli":
					cli = true;
				}
			}
		}
		
		if (cli) {
			//Command line interface
			CLI cmdLine = new CLI(c, verbose);
			cmdLine.init();
		} else {
			//Paul, start your GUI here
		}
		
		/*
		try {
			//c.authenticate(Authenticate.proto.IMAP, "ebull", "foobar");
			String testName = "amani@gmail.com";
			String testPwd = "amani";
			if (c.authenticate(testName, testPwd)) {
				System.out.println("Authenticated as " + c.getUname());
			} else {
				System.out.println("Authenticated failed");
				return;
			}
			
			System.out.println("---------requesting update-----------");
			c.update();
		} catch (ClientRequestException e) {
			System.out.println(e.getMessage());
		}
		*/
	}

}
