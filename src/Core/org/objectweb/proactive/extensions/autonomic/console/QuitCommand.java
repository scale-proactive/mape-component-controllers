package org.objectweb.proactive.extensions.autonomic.console;

public class QuitCommand extends AbstractCommand {

	public QuitCommand(Console console) {
		super(console);
	}

	@Override
	public String getName() {
		return "quit";
	}

	@Override
	public String getInfo() {
		return "Close the console and exit.";
	}

	@Override
	public void execute(String args) throws Exception {
		console.quit();
	}



}
