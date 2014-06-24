package org.objectweb.proactive.extensions.autonomic.controllers.analysis;

import java.util.ArrayList;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;


public interface AnalyzerController {

	public final static String ITF_NAME = "analysis-service-nf";

	public void addRule(String name, Rule rule);
	public void removeRule(String name);

	/**
	 * Checks the rule identified by the name "ruleName"
	 * @param ruleName name used to identified the rule
	 * @return the alarm threw in the rule checking
	 */
	public Wrapper<Alarm> checkRule(String ruleName);

	/**
	 * Return a list with the names of all the existent rules.
	 * @return list of names
	 */
	public Wrapper<ArrayList<String>> getRuleNames();
}
