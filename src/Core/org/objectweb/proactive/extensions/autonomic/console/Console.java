package org.objectweb.proactive.extensions.autonomic.console;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import jline.console.ConsoleReader;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.exceptions.NotAutonomicException;

public class Console implements Runnable {

	// Communication with the component through the executor controller
	protected ExecutorController executorController;
	
	// Communication with the user through JLine library
	protected ConsoleReader reader;

	// Loaded commands: name -> implementation
	private Map<String, AbstractCommand> commands;

	// Prompt
	protected String PROMPT = "GCMScript>";

	boolean finished;

	/**
	 * Initialize the console on the desired autonomic component
	 * 
	 * @param comp
	 * @throws IOException 
	 */
	public Console(Component comp) throws NotAutonomicException, IOException {
		
		this.currentComponent(comp);

		commands = new HashMap<String, AbstractCommand>();
		reader = new ConsoleReader();
		reader.setPrompt(PROMPT);
		finished = false;

		registerCommands();
	}

	public void currentComponent(Component comp) throws NotAutonomicException {
		try {
			executorController = Remmos.getExecutorController(comp);
			executorController.execute("true();"); // ping, to ensure that executor is initialized
		} catch (NoSuchInterfaceException e) {
			throw new NotAutonomicException(e);
		}
		try {
			String compName = GCM.getNameController(comp).getFcName();
			PROMPT = "GCMScript@" + compName + "$ ";
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
	}

	protected void registerCommands() {
		registerCommand(new HelpCommand(this));
		registerCommand(new QuitCommand(this));
		registerCommand(new ExecCommand(this));
		registerCommand(new ChangeCommand(this));
	}

	protected void registerCommand(AbstractCommand command) {
		commands.put(command.getName(), command);
	}

	@Override
	public void run() {
		while (!finished) {
			String input = getInput();
			processInput(input);
		}
	}

	protected String getInput() {
		try {
			String input = reader.readLine(PROMPT);
			return input != null ? input.trim() : null;
		} catch (IOException e) {
			printError("IO error while reading user input", e);
		}
		return null;
	}

	protected void processInput(String input) {
		if (input != null && input.length() == 0)
			return;
		
		if (input == null) {
			processCommand("quit", "");
		} else if (input.startsWith(":")) {
			int i = input.indexOf(' ');
			if (i != -1) {
				processCommand(input.substring(1, i), input.substring(i + 1));
			} else {
				processCommand(input.substring(1), "");
			}
		} else {
			processCommand("exec", input);
		}
	}

	protected void processCommand(String commandName, String args) {
		AbstractCommand command = commands.get(commandName);
		if (command == null) {
			printError("Command \":" + commandName + "\" not found. Please use \":help\" for more information.");
			return;
		}
		
		try {
			command.execute(args);
		} catch (Exception e) {
			printError("Fail to execute \":" + commandName + "\"", e);
		}
	}

	protected void printMsg(String msg) {
		try {
			reader.putString(msg);
			reader.println();
		} catch (IOException e) {
			System.err.print("IO error in the console: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void printError(String msg) {
		printMsg("Error: " + msg);
	}

	protected void printError(String msg, String cause) {
		printMsg("Error: " + msg);
		printMsg("Cause: " + cause);
	}

	protected void printError(String msg, Throwable cause) {
		cause.printStackTrace();
		printMsg("Error: " + msg);
		printMsg("Cause: " + cause.getMessage());
	}

	protected void quit() {
		finished = true;
		reader.shutdown();
	}

	/**
	 * Returns a read-only collection with the current registered commands
	 * @return
	 */
	protected Collection<AbstractCommand> getCommands() {
		return Collections.unmodifiableCollection(commands.values());
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("c", true, "component url");
		options.addOption("l", false, "print remi objects");
		options.addOption("h", true, "host");
		options.addOption("p", true, "port");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);

		int PORT = 1099;
		if(cmd.hasOption("p")) {
			PORT = Integer.parseInt(cmd.getOptionValue("p"));
		}

		String HOST = "localhost";
		if (cmd.hasOption("h")) {
			HOST = cmd.getOptionValue("h");
		}

		if (cmd.hasOption("c")) {
			String url = cmd.getOptionValue("c");
			try {
				Component c = Fractive.lookup(url);
				String name = ((PAComponent)c).getComponentParameters().getName();
				System.out.println("Opening console on ["+name+"] @ ["+url+"]");
				(new Console(c)).run();
			} catch (NamingException ne) {
				System.err.println("url not found: " + url);
			}
		}

		if (cmd.hasOption("l")) {
			Registry registry = LocateRegistry.getRegistry(HOST, PORT);
			String[] boundNames = registry.list();
			for (String name : boundNames) { System.out.println("\t" + name); }
		}
		
	}
}
