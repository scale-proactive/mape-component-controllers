package org.objectweb.proactive.extensions.autonomic.console;

public abstract class AbstractCommand {

	Console console;

	public AbstractCommand(Console console) {
		this.console = console;
	}

	public abstract String getName();

	public abstract String getInfo();

	public abstract void execute(String args) throws Exception;

}
