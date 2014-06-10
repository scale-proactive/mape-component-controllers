package examples.md5cracker.plans;

import org.objectweb.proactive.extra.component.mape.analysis.Alarm;
import org.objectweb.proactive.extra.component.mape.execution.ExecutorController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.planning.Plan;
import org.objectweb.proactive.extra.component.mape.utils.ObjectWrapper;
import org.objectweb.proactive.extra.component.mape.utils.WrongValueException;

import examples.md5cracker.actions.AddSolverAction;
import examples.md5cracker.actions.RemoveSolverAction;
import examples.md5cracker.rules.MaxPerformanceRule;
import examples.md5cracker.rules.MinPerformanceRule;

/**
 * This plan is assumed to be under Cracker composite component
 *
 */
public class QoSPlan extends Plan {

	private static final long serialVersionUID = 1L;
	private int maxWorkers, maxSolvers;
	private long lastTime, delay;

	public QoSPlan(int maxNumOfWorkers, int maxNumOfSolvers, long delay) {
		maxWorkers = maxNumOfWorkers;
		maxSolvers = maxNumOfSolvers;
		this.lastTime = System.currentTimeMillis();
		this.delay = delay;
	}


	@Override
	public void planActionFor(String ruleName, Alarm alarm, MonitorController monitor, ExecutorController executor) {

		if ( ruleName.equals(MinPerformanceRule.DEFAULT_NAME) ) {
			minPerformanceHandler(alarm, monitor, executor);
		} else if ( ruleName.equals(MaxPerformanceRule.DEFAULT_NAME) ) {
			maxPerformanceHandler(alarm, monitor, executor);
		}
		
	}

	private void maxPerformanceHandler(Alarm alarm, MonitorController monitor, ExecutorController executor) {
		
		if ( alarm != Alarm.VIOLATION ) return;
		
		// Check if enough time has passed since the last system change
		synchronized (this) {
			if (System.currentTimeMillis() - lastTime < delay) {
				return; // is not the moment yet, try later.
			}
			lastTime = Long.MAX_VALUE; // this ensures that nobody else will met the delay condition.
		}
		System.out.println("[PLANNER_CONTROLLER] Planning action for performance decrease... ");

		// Try to delete a worker
		boolean value = false;
		try {
			System.out.println("[PLANNER_CONTROLLER] trying to remove worker...");
			ObjectWrapper result = executor.execute("remove-worker($this);");
			value = (boolean) result.getObject();
		} catch (WrongValueException e2) {
			e2.printStackTrace();
		}

		if (value) {
			System.out.println("[PLANNER_CONTROLLER] worker removed.");
			lastTime = System.currentTimeMillis();
			return;
		} else {
			System.out.println("[PLANNER_CONTROLLER] can't remove worker...");
		}

		
		double numOfSolvers = 0;
		try {
			ObjectWrapper result = executor.execute("value($this/child::CrackerManager/attribute::numberOfSolvers);");
			numOfSolvers = (double) result.getObject();
		} catch (WrongValueException e1) {
			e1.printStackTrace();
		}

		if (numOfSolvers > 1) {

			value = false;

			try {
				System.out.println("[PLANNER_CONTROLLER] trying to remove solver...");
				ObjectWrapper result = executor.executeAction(RemoveSolverAction.DEFAULT_NAME);
				value = (boolean) result.getObject();
			} catch (WrongValueException e) {
				e.printStackTrace();
			}

			if (value) {
				System.out.println("[PLANNER_CONTROLLER] solver removed.");
			} else {
				System.out.println("[PLANNER_CONTROLLER] solver remotion failed...");
			}
		} else {
			System.out.println("[PLANNER_CONTROLLER] nothing to do...");
		}
		
		lastTime = System.currentTimeMillis();
	}


	private void minPerformanceHandler(Alarm alarm, MonitorController monitor, ExecutorController executor) {

		if ( alarm != Alarm.VIOLATION ) return;
		
		// Check if enough time has passed since the last system change
		synchronized (this) {
			if (System.currentTimeMillis() - lastTime < delay) {
				return; // is not the moment yet, try later.
			}
			lastTime = Long.MAX_VALUE; // this ensures that nobody else will met the delay condition.
		}
		System.out.println("[PLANNER_CONTROLLER] Planning action for increase the performance... ");

		// Try to improve a solver
		System.out.println("[PLANNER_CONTROLLER] trying to add a worker...");
		ObjectWrapper result = executor.execute("improve-solvers($this, " + maxWorkers + ");");
		boolean value;
		try {
			value = (boolean) result.getObject();
		} catch (WrongValueException e2) {
			e2.printStackTrace();
			value = false;
		}

		if (value) {
			System.out.println("[PLANNER_CONTROLLER] new worker added.");
			lastTime = System.currentTimeMillis();
			return;
		}
		
		// Try to add a new solver
		System.out.println("[PLANNER_CONTROLLER] trying to add new solver...");
		result = executor.execute("value($this/child::CrackerManager/attribute::numberOfSolvers);");
		double numOfSolvers;
		try {
			numOfSolvers = (double) result.getObject();
		} catch (WrongValueException e1) {
			e1.printStackTrace();
			numOfSolvers = maxSolvers;
		}

		if (maxSolvers > numOfSolvers) {
			System.out.println("[PLANNER_CONTROLLER] adding new solver...");
			result = executor.executeAction(AddSolverAction.DEFAULT_NAME);
			try {
				value = (boolean) result.getObject();
			} catch (WrongValueException e) {
				e.printStackTrace();
				value = false;
			}

			if (value) {
				System.out.println("[PLANNER_CONTROLLER] new solver added.");
			} else {
				System.out.println("[PLANNER_CONTROLLER] new solver addedition failed...");
			}
		} else {
			System.out.println("[PLANNER_CONTROLLER] nothing to do...");
		}
		
		lastTime = System.currentTimeMillis();
	}
}
