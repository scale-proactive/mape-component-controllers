package org.objectweb.proactive.extensions.autonomic.controllers;

public interface ACConstants {

	public static final String MONITOR_CONTROLLER = "autonomic-monitor-controller";
	public static final String ANALYZER_CONTROLLER = "analyzer-controller";
	public static final String PLANNER_CONTROLLER = "planner-controller";
	public static final String EXECUTOR_CONTROLLER = "executor-controller";
	
	public static final String INTERNAL_SERVER_NFITF = "inernal-server-" + MONITOR_CONTROLLER;
	public static final String INTERNAL_CLIENT_SUFFIX = "-internal-" + MONITOR_CONTROLLER;
	public static final String EXTERNAL_CLIENT_SUFFIX = "-external-" + MONITOR_CONTROLLER;

	
	public static final String ITF_EVENT_LISTENER = "event-listener-nf";
	public static final String ITF_METRIC_STORE = "metric-store-nf";;
	public static final String ITF_REMMOS_EVENT_LISTENER = "remmos-event-listener-nf";
	public static final String ITF_RECORD_STORE = "record-store-nf";

	public static final String COMP_EVENT_LISTENER_NAME = "event-listener-comp-nf";
	public static final String COMP_METRIC_STORE_NAME = "metric-store-comp-nf";
	public static final String COMP_MONITOR_MANAGER_NAME = "monitor-manager-comp-nf";
	public static final String COMP_RECORD_STORE_NAME = "record-store-comp-nf";
	
	public static final String COMP_ANALYZER_NAME = "analyzer-controller-comp-nf";
	public static final String COMP_PLANNER_NAME = "planner-controller-comp-nf";
	public static final String COMP_EXECUTOR_NAME = "executor-ontroller-comp-nf";
	
}
