package org.objectweb.proactive.extensions.autonomic.console;

public class HelpCommand extends AbstractCommand {

	public HelpCommand(Console console) {
		super(console);
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getInfo() {
		return "Shows this information";
	}

	@Override
	public void execute(String args) throws Exception {
		String msg = "This is a console to execute GCMScript commands directly over the system.\n";
		msg += "Additionaly provide some utils commands that can be call by using the color\n";
		msg += "\":\" prefix. The list of available commands are listed below:\n"; 
		console.printMsg(msg);
		
		console.printMsg("\t(COMMAND)\t(INFO)");

		for (AbstractCommand c : console.commands.values()) {
			console.printMsg("\t:" + c.getName() + "\t" + c.getInfo());
		}
		
		console.printMsg("");
	}

}
