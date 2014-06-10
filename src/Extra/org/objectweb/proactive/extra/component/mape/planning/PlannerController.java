package org.objectweb.proactive.extra.component.mape.planning;

public interface PlannerController {

    public static final String PLANNER_CONTROLLER = "planner-controller";
    public static final String ITF_NAME = "planning-service-nf";

    public void setPlan(Plan plan);

}
