package org.objectweb.proactive.extra.component.mape.analysis;

import org.objectweb.proactive.extra.component.mape.analysis.Rule;

public interface AnalysisController {

	public final static String ITF_NAME = "analysis-controller-nf";

	public void addRule(String name, Rule rule, String actionName);
	public void addRule(String name, Rule rule);
	public void removeRule(String name);
	
	/**
	 * Check if a rule is met. NOTE: this method will not trigger the execution of the associated action,
	 * if such association existed.
	 * @param ruleName
	 * @return
	 */
	public Boolean checkRule(String ruleName);

}
