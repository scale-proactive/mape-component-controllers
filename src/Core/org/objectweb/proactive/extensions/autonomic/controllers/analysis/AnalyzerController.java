package org.objectweb.proactive.extensions.autonomic.controllers.analysis;

import java.util.HashSet;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;


public interface AnalyzerController {

	public final static String ITF_NAME = "analysis-service-nf";

	/**
	 * Adds a new rule to analyzer controller
	 * @param name name of the rule
	 * @param rule the rule
	 * @return true if success, false otherwise
	 */
	public Wrapper<Boolean> addRule(String name, Rule rule);

	/**
	 * Removes a rule
	 * @param name the name of the rule to be removed
	 * @return true if success
	 */
	public Wrapper<Boolean> removeRule(String name);

	/**
	 * Checks the rule identified by the name "ruleName"
	 * @param ruleName name used to identified the rule
	 * @return the alarm threw in the rule checking
	 */
	public Wrapper<Alarm> checkRule(String ruleName);

	/**
	 * Returns a set with the names of all the existent rules.
	 * @return set of names
	 */
	public Wrapper<HashSet<String>> getRulesNames();

	/**
	 * Returns a set with all the subscriptions (metric names) of one rule.
	 * @param ruleName name of the rule to query
	 * @return set with subscriptions (metric names)
	 */
	public Wrapper<HashSet<String>> getRuleSubscriptions(String ruleName);

	/**
	 * Subscribe a rule to a metric
	 * @param ruleName name of the rule
	 * @param metricName name of the metric to subscribe to
	 * @return true if success, false otherwise
	 */
	public Wrapper<Boolean> subscribeRuleTo(String ruleName, String metricName);

}
