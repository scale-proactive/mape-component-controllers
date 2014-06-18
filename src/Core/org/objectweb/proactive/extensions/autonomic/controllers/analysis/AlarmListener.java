package org.objectweb.proactive.extensions.autonomic.controllers.analysis;


public interface AlarmListener {

	public static final String ITF_NAME = "alarm-listener-service-nf";

	public void listenAlarm(String ruleName, Alarm alarm);

}
