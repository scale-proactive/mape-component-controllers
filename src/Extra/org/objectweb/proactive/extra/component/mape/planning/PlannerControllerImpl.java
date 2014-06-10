package org.objectweb.proactive.extra.component.mape.planning;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.extra.component.mape.analysis.Alarm;
import org.objectweb.proactive.extra.component.mape.analysis.AlarmListener;
import org.objectweb.proactive.extra.component.mape.execution.ExecutorController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;

public class PlannerControllerImpl extends AbstractPAComponentController implements PlannerController,
		AlarmListener, BindingController {

	private static final long serialVersionUID = 1L;

	private MonitorController monitor;
	private ExecutorController executor;
	
	// TODO: Support multiples plans
	private Plan plan;


	@Override
	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	@Override
	public void listenAlarm(String ruleName, Alarm alarm) {
		plan.planActionFor(ruleName, alarm, monitor, executor);
	}

	@Override
	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException {
		if (clientItfName.equals(MonitorController.ITF_NAME)) {
			monitor = (MonitorController) serverItf;
		} else if (clientItfName.equals(ExecutorController.ITF_NAME)) {
			executor = (ExecutorController) serverItf;
		} else {
			throw new NoSuchInterfaceException("[@PlannerController] " + clientItfName);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] { MonitorController.ITF_NAME, ExecutorController.ITF_NAME };
	}


	@Override
	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(MonitorController.ITF_NAME)) {
			return monitor;
		} else if (clientItfName.equals(ExecutorController.ITF_NAME)) {
			return executor;
		}
		throw new NoSuchInterfaceException("[@PlannerController] " + clientItfName);
	}

	@Override
	public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(MonitorController.ITF_NAME)) {
			monitor = null;
		} else if (clientItfName.equals(ExecutorController.ITF_NAME)) {
			executor = null;
		} else {
			throw new NoSuchInterfaceException("[@PlannerController] " + clientItfName);
		}
	}

}
