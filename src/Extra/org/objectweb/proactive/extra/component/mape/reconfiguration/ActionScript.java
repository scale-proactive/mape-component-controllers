package org.objectweb.proactive.extra.component.mape.reconfiguration;

class ActionScript implements Executable {

	private static final long serialVersionUID = 1L;
	private String script;

	
	public ActionScript(String script) {
		this.script = script;
	}

	protected String getScript() {
		return script;
	}

}
