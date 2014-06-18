package org.objectweb.proactive.extensions.autonomic.controllers.planning;

public interface PlannerController {

    public static final String ITF_NAME = "planning-service-nf";

    public void setPlan(Plan plan);

}
