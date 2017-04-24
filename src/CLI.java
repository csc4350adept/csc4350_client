import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class CLI {
	Scanner stdin = new Scanner(new BufferedInputStream(System.in));
	String banner = "Welcome to ADEPT mail client\nType a command or ? for options";
	String cliString = "adept> ";
	String quitString = "exit";
	HashMap<String, String> cmdList = new HashMap<String, String>();
	private boolean verbose = false;
	private boolean loggedIn = false;
	Client client;
	
	public CLI(Client client, boolean verbose) {
		this.client = client;
		this.verbose = verbose;
		cmdList = generateCmdList();
	}
	
	public void init() {
		print(banner);
		String line = "";
		String resp;
		System.out.print("\n" + cliString);
		System.out.flush();
		while (stdin.hasNextLine()) {
			line = stdin.nextLine().trim();
			if (line.equals(quitString)) break;
			resp = commandParser(line);
			print(resp);
			System.out.print("\n" + cliString);
			System.out.flush();
		}
	}
	
	private String commandParser(String input) {
		String resp = "Type a command or ? for options";
		String command = input.split("\\s")[0];
		
		
		switch (command) {
			case "?":
				resp = help();
				break;
			case "login":
				resp = login(input);
				break;
			case "logout":
				resp = logout();
				break;
			case "settings":
				resp = settings(input);
				break;
			case "update":
				resp = update();
				break;
			case "view":
				resp = view(input);
				break;
			case "send":
				resp = send(input);
				break;
			case "mkfolder":
				resp = mkFolder(input);
				break;
			case "rmfolder":
				resp = rmFolder(input);
				break;
			case "namefolder":
				resp = nameFolder(input);
				break;
			case "mailboxes":
				resp = listMailboxNames();
				break;
			default:
				return resp;
		}
		return resp;
	}
	
	private String help() {
		ArrayList<String> result = new ArrayList<String>();
		for (String cmd : cmdList.keySet()) result.add(cmdList.get(cmd));
		return String.join("\n", result);
	}
	
	private String login(String command) {
		String resp = "Invalid login command. Type ? for a list of valid commands and arguments";
		ArrayList<String> commandParts = new ArrayList<String>(Arrays.asList(command.split("\\s")));
		if (commandParts.size() == 3) {
			String username = commandParts.get(1);
			String password = commandParts.get(2);
			try {
				if (client.authenticate(username, password)) {
					resp = "Login successful";
					loggedIn = true;
				} else {
					resp = "Login unsuccessful";
				}
			} catch (ClientRequestException e) {
				resp = "Login failed. " + e.getMessage();
			}
		}
		return resp;
	}
	
	private String logout() {
		client.clearAuthentication();
		loggedIn = false; //Commands will still fail if they don't have auth set in client, but be graceful and track it here
		return "Logged out";
	}
	
	private String update() {
		String resp;
		try {
			if (client.update()) resp = "Update successful";
			else resp = "Update unsuccessful"; //informative, I know...
		} catch (ClientRequestException e) {
			resp = e.getMessage();
		}
		return resp;
	}
	
	private String listMailboxNames() {
		String resp;
		try {
			ArrayList<String> mailboxes = client.getAllMailboxNames(client.getUname());
			resp = String.join("\n", mailboxes);
		} catch (ClientRequestException e) {
			resp = e.getMessage();
		}
		return resp;
	}
	
	private String settings(String command) {
		String resp = "Invalid setting command. Type ? for a list of valid commands and arguments";
		if (!loggedIn) return "Must be logged in to view or change settings";
		ArrayList<String> commandParts = new ArrayList<String>(Arrays.asList(command.split("\\s")));
		if (commandParts.size() == 1) {
			//Display settings
			ArrayList<String> result = new ArrayList<String>();
			try {
				result.add(String.format("Username: %s", client.getUname()));
				result.add(String.format("Server: %s", client.getServer(client.getUname())));
				result.add(String.format("SMTP: %s", client.getSMTP(client.getUname())));
				result.add(String.format("IMAP: %s", client.getIMAP(client.getUname())));
				result.add(String.format("Key (currently not implemented): %s", client.getUserKey(client.getUname())));
			} catch (ClientRequestException e) {
				resp = e.getMessage();
			}
			resp = String.join("\n", result);
		} else if (commandParts.size() == 3) {
			//Change a particular setting
			String setting = commandParts.get(1).toLowerCase();
			String value = commandParts.get(2);
			String success = "Setting changed";
			String failure = "Setting not changed";
			switch (setting) {
				case "username":
					resp = "Changing usernames is planned for ADEPT Client 2.0";
					break;
				case "server":
					try {
						if (client.setServer(client.getUname(), value)) resp = success;
						else resp = failure;
					} catch (ClientRequestException e) {
						resp = e.getMessage();
					}
					break;
				case "smtp":
					try {
						if (client.setSMTP(client.getUname(), value)) resp = success;
						else resp = failure;
					} catch (ClientRequestException e) {
						resp = e.getMessage();
					}
					break;
				case "imap":
					try {
						if (client.setIMAP(client.getUname(), value)) resp = success;
						else resp = failure;
					} catch (ClientRequestException e) {
						resp = e.getMessage();
					}
					break;
				case "key":
					try {
						if (client.setKey(client.getUname(), value)) resp = success;
						else resp = failure;
					} catch (ClientRequestException e) {
						resp = e.getMessage();
					}
					break;
				default:
					resp = "Invalid setting";
			}
		}
		return resp;
	}
	
	private String view(String command) {
		String resp = "Exiting view";
		ArrayList<String> commandParts = new ArrayList<String>(Arrays.asList(command.split("\\s")));
		//Display all emails
		//Use a displayEmails(ArrayList<String>) function which displays emails 10 at a time
		//Advances by hitting "n" or stops by hitting "q"
		ArrayList<String> ids = new ArrayList<String>();
		try {
			if (commandParts.size() == 1) {
				ids = client.getAllEmailIds(client.getUname());
			}
			if (commandParts.size() == 2) {
				String option = commandParts.get(1).toLowerCase();
				switch (option) {
					//Display only read emails
					case "read":
						ids = client.getReadEmailIds(client.getUname());
						break;
					//Display only unread emails
					case "unread":
						ids = client.getUnreadEmailIds(client.getUname());
						break;
					default:
						ids = client.getEmailsByMailbox(client.getUname(), commandParts.get(1));
				}
			}
			if (commandParts.size() == 3) {
				String option = commandParts.get(1).toLowerCase();
				String mailbox = commandParts.get(2);
				boolean read;
				switch (option) {
					//Display only read emails
					case "read":
						read = true;
						ids = client.getEmailsByMailbox(client.getUname(), mailbox, read);
						break;
					//Display only unread emails
					case "unread":
						read = false;
						ids = client.getEmailsByMailbox(client.getUname(), mailbox, read);
						break;
				}
			}
			displayIdsByPage(ids);
		} catch (ClientRequestException e) {
			resp = e.getMessage();
		}
		return resp;
	}
	
	private void displayIdsByPage(ArrayList<String> ids) {
		if (ids.size() < 1) return;
		int pageSize = 10; //Number of results to display per page
		int pages = (int) Math.ceil((double) ids.size() / pageSize);
		int page = 0;
		ArrayList<String> lines = new ArrayList<String>();
		HashMap<Integer, String> idResolver = new HashMap<Integer, String>();
		while (page <= pages) {
			lines.add(String.format("Displaying %d of %d pages", page + 1, pages));
			int cursor = page * pageSize;
			while (cursor < ((page * pageSize) + pageSize) && cursor < ids.size()) {
				String subject;
				String mailbox;
				try {
					mailbox = client.getEmailMailbox(ids.get(cursor));
				} catch (ClientRequestException e) {
					mailbox = "Could not retrieve mailbox for email " + ids.get(cursor);
				}
				try {
					subject = client.getEmailSubject(ids.get(cursor));
				} catch (ClientRequestException e) {
					subject = "Could not retrieve subject header for email " + ids.get(cursor);
				}
				String line = String.format("%s (%s):    %s", cursor, mailbox, subject);
				idResolver.put(new Integer(cursor), ids.get(cursor));
				lines.add(line);
				cursor++;
			}
			lines.add("Enter an email id to view it, n to go to the next page, or q to stop browsing results");
			print(String.join("\n", lines));
			System.out.print("\n" + cliString + "-view> ");
			System.out.flush();
			String line;
			boolean quit = false;
			while (stdin.hasNextLine()) {
				line = stdin.nextLine().trim();
				if (line.matches("[0-9]+")) {
					displayEmail(idResolver.get(Integer.parseInt(line)));
				}
				if (line.equals("q")) quit = true;
				if (line.equals("n") || line.equals("q")) break;
				print(String.join("\n", lines));
				System.out.print("\n" + cliString + "-view> ");
				System.out.flush();
			}
			if (quit) break;
			page++;
		}
	}
	
	private void displayEmail(String id) {
		ArrayList<String> email = new ArrayList<String>();
		try {
			email.add("DATE: " + client.getEmailDate(id));
			email.add("TO: " + client.getEmailTo(id));
			email.add("FROM: " + client.getEmailFrom(id));
			email.add("SUBJECT: " + client.getEmailSubject(id));
			email.add("BODY:\n" + client.getEmailBody(id));
			email.add("END OF EMAIL\n");
			client.setEmailRead(id);
		} catch (ClientRequestException e) {
			print("Could not retrieve email");
			return;
		}
		print(String.join("\n", email));
		//Edit Email Options
		print("mvemail <mailbox> to move the email or rmemail to delete it or any other key to continue");
		System.out.print("\n" + cliString + String.format("-email#%s> ", id));
		System.out.flush();
		if (stdin.hasNextLine()) {
			String line = stdin.nextLine();
			if (line.startsWith("mvemail")) {
				print(mvEmail(line, id));
			} else if (line.equals("rmemail")) {
				print(rmEmail(id));
			}
		}
	}
	
	private String send(String input) {
		String resp = "Invalid send command";
		if (input.split("\\s").length > 1) return "Invalid send arguments";
		HashMap<String, String> emailData = new HashMap<String, String>();
		
		emailData.put("from", client.getAuth().getUname());
		emailData.put("date", new SimpleDateFormat("yyyy.mm.dd").format(new java.util.Date()));
		
		print("Enter recipient (if multiple recipients, separate by commas)");
		System.out.print("\n" + cliString + "-send> ");
		System.out.flush();
		if (stdin.hasNextLine())
			emailData.put("to", stdin.nextLine().trim());
		
		print("Enter subject");
		System.out.print("\n" + cliString + "-send> ");
		System.out.flush();
		if (stdin.hasNextLine())
			emailData.put("subject", stdin.nextLine().trim());
		
		print("Enter body, terminated with a \".\" on a single line");
		ArrayList<String> bodyInput = new ArrayList<String>();
		while (stdin.hasNextLine()) {
			String line = stdin.nextLine().trim();
			if (line.equals(".")) break;
			bodyInput.add(line);
		}
		emailData.put("body", String.join("\n", bodyInput));
		
		try {
			if(EditEmail.sendEmail(client, emailData.get("date"), emailData.get("to"), emailData.get("from"), emailData.get("subject"), emailData.get("body")))
				resp = "Email sent";
		} catch (ClientRequestException e) {
			resp = "Server command failed " + e.getMessage();
		}
		return resp;
	}
	
	private String mkFolder(String input) {
		String resp = "Invalid mkFolder command";
		ArrayList<String> respParts = new ArrayList<String>(Arrays.asList(input.split("\\s")));
		if (respParts.size() != 2) return "Invalid mkFolder arguments";
		String mailbox = respParts.get(1);
		
		try {
			if (EditEmail.createMailbox(client, mailbox))
				resp = "Mailbox created";
		} catch (ClientRequestException e) {
			resp = e.getMessage();
		}
		return resp;
	}
	
	private String rmFolder(String input) {
		String resp = "Invalid rmFolder command";
		ArrayList<String> respParts = new ArrayList<String>(Arrays.asList(input.split("\\s")));
		if (respParts.size() != 2) return "Invalid rmFolder arguments";
		String mailbox = respParts.get(1);
		
		try {
			if (EditEmail.deleteMailbox(client, mailbox))
				resp = "Mailbox deleted";
		} catch (ClientRequestException e) {
			resp = e.getMessage();
		}
		return resp;
	}
	
	private String nameFolder(String input) {
		String resp = "Invalid nameFolder command";
		ArrayList<String> respParts = new ArrayList<String>(Arrays.asList(input.split("\\s")));
		if (respParts.size() != 3) return "Invalid nameFolder arguments";
		String mailbox = respParts.get(1);
		String newname = respParts.get(2);
		
		try {
			if (EditEmail.renameMailbox(client, mailbox, newname))
				resp = "Mailbox renamed";
		} catch (ClientRequestException e) {
			resp = e.getMessage();
		}
		return resp;
	}
	
	private String mvEmail(String input, String emailId) {
		String resp = "Invalid mvEmail command";
		ArrayList<String> inputParts = new ArrayList<String>(Arrays.asList(input.split("\\s")));
		if (inputParts.size() < 2) return "Invalid argument. Enter mvemail <mailbox>";
		String mailbox = String.join(" ", inputParts.subList(1, inputParts.size()));
		
		try {
			if (EditEmail.moveEmail(client, emailId, mailbox))
				resp = "Email moved";
		} catch (ClientRequestException e) {
			resp = e.getMessage();
		}
		return resp;
	}
	
	private String rmEmail(String emailId) {
		String resp = "Invalid nameFolder command";
		try {
			if (EditEmail.deleteEmail(client, emailId))
				resp = "Email deleted";
		} catch (ClientRequestException e) {
			resp = e.getMessage();
		}
		return resp;
	}
	
	private HashMap<String, String> generateCmdList() {
		HashMap<String, String> result = new HashMap<String, String>();
		result.put("?", 		"?                                  Gets a list of valid commands and arguments");
		result.put("login", 	"login <username> <password>        Logs in with a username and password. Valid authentication is required for all other commands");
		result.put("logout", 	"logout                             Logs out of the current session");
		result.put("settings", 	"settings [<setting> <value>]       Views user settings or updates a specific setting with a new value");
		result.put("update", 	"update                             Forces an update with the server");
		result.put("view", 		"view [read | unread] [<mailbox>]   views a list of all emails, or a list of only read emails, or a list of only unread emails and display an email from the resulting set");
		result.put("send", 		"send                               Enters a dialogue for sending an email");
		result.put("mkfolder", 	"mkfolder <mailbox>                 Creates a mailbox with the specified name");
		result.put("rmfolder", 	"rmfolder <mailbox>                 Deleted a mailbox with the specified name");
		result.put("namefolder","namefolder <mailbox> <mailbox>     Renames a mailbox with the specified name to a new name");
		result.put("mailboxes", "mailboxes                          Lists all mailbox names");
		return result;
	}
	private void print(String s) {
		System.out.println(s);
		System.out.flush();
	}
	
	@SuppressWarnings("unused")
	private void printv(String s) {
		if (verbose) System.out.println("\n" + s + "\n");
		System.out.flush();
	}
}
