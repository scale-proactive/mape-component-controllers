package org.objectweb.proactive.extensions.autonomic.controllers;

public interface ACConstants {

	public static final String MONITOR_CONTROLLER = "autonomic-monitor-controller";
	public static final String ANALYZER_CONTROLLER = "analyzer-controller";
	public static final String PLANNER_CONTROLLER = "planner-controller";
	public static final String EXECUTOR_CONTROLLER = "executor-controller";
	
	public static final String INTERNAL_SERVER_NFITF = "inernal-server-" + MONITOR_CONTROLLER;
	public static final String INTERNAL_CLIENT_SUFFIX = "-internal-" + MONITOR_CONTROLLER;
	public static final String EXTERNAL_CLIENT_SUFFIX = "-external-" + MONITOR_CONTROLLER;
	
}
