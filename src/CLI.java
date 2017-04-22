import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class CLI {
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
		
		Scanner stdin = new Scanner(new BufferedInputStream(System.in));
		ArrayList<String> input = new ArrayList<String>();
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
	
	private String settings(String command) {
		String resp = "Invalid setting command. Type ? for a list of valid commands and arguments";
		if (!loggedIn) return "Must be logged in to view or change settings";
		ArrayList<String> commandParts = new ArrayList<String>(Arrays.asList(command.split("\\s")));
		if (commandParts.size() == 1) {
			//Display settings
			ArrayList<String> result = new ArrayList<String>();
			result.add(String.format("Username: %s", client.getUname()));
			result.add(String.format("Server: %s", client.getServer(client.getUname())));
			result.add(String.format("SMTP: %s", client.getSMTP(client.getUname())));
			result.add(String.format("IMAP: %s", client.getIMAP(client.getUname())));
			result.add(String.format("Key (currently not implemented): %s", client.getUserKey(client.getUname())));
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
					resp = "Invalid setting.";
			}
		return resp;
		}
		
		
		
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
	
	private HashMap<String, String> generateCmdList() {
		HashMap<String, String> result = new HashMap<String, String>();
		result.put("?", "? Gets a list of valid commands and arguments");
		result.put("login", "login <username> <password> Logs in with a username and password. Valid authentication is required for all other commands");
		result.put("logout", "logout Logs out of the current session");
		result.put("settings", "settings [<setting> <value>] Views user settings or updates a specific setting with a new value");
		result.put("update", "update Forces an update with the server");
		
		return result;
	}
	private void print(String s) {
		System.out.println(s);
		System.out.flush();
	}
	
	private void printv(String s) {
		if (verbose) System.out.println("\n" + s + "\n");
		System.out.flush();
	}
}
