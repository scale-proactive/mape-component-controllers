package org.objectweb.proactive.extensions.autonomic.controllers.analysis;

import java.util.NoSuchElementException;

import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Rule;

public interface AnalyzerController {

	public final static String ITF_NAME = "analysis-service-nf";

	public void addRule(String name, Rule rule);
	public void removeRule(String name);
	
	/**
	 * Checks the rule identified by the name "ruleName"
	 * @param ruleName name used to identified the rule
	 * @return the alarm threw in the rule checking
	 */
	public Alarm checkRule(String ruleName) throws NoSuchElementException;

}
