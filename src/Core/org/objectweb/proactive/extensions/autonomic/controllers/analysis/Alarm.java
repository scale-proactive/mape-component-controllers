package org.objectweb.proactive.extensions.autonomic.controllers.analysis;

public enum Alarm {
	OK("OK"), WARNING("WARNING"), VIOLATION("VIOLATION"), FAIL("FAIL");

    private String alarm;

    private Alarm(String alarm) {
        this.alarm = alarm;
    }
   
    @Override
    public String toString() {
        return alarm;
    }

}
