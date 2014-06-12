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

	/**
	 *  Try do decrease the system performance
	 * @param alarm
	 * @param monitor
	 * @param executor
	 * @return 
	 */
	private synchronized void maxPerformanceHandler(Alarm alarm, MonitorController monitor, ExecutorController executor) {
		
		if ( alarm != Alarm.VIOLATION ) return;
		
		// Check if enough time has passed since the last system change
		if (System.currentTimeMillis() - lastTime < delay) {
			return; // is not the moment yet, try later.
		}

		try {
			System.out.println("[PLANNER_CONTROLLER] trying to remove worker...");
			ObjectWrapper result = executor.execute("remove-worker($this);");
			if (result.isValid() && (boolean) result.getObject()) {
				System.out.println("[PLANNER_CONTROLLER] worker removed.");
				lastTime = System.currentTimeMillis();
				return;
			}
		} catch (WrongValueException e2) {
			e2.printStackTrace();
		}


		try {
			System.out.println("[PLANNER_CONTROLLER] trying to remove solver...");
			ObjectWrapper result = executor.execute("value($this/child::CrackerManager/attribute::numberOfSolvers);");
			if (result.isValid() && 1 < (double) result.getObject()) {
				try {
					result = executor.executeAction(RemoveSolverAction.DEFAULT_NAME);
					if (result.isValid() && (boolean) result.getObject()) {
						System.out.println("[PLANNER_CONTROLLER] solver removed.");
					}
				} catch (WrongValueException e) {
					e.printStackTrace();
				}
			}
		} catch (WrongValueException e1) {
			e1.printStackTrace();
		}

		System.out.println("[PLANNER_CONTROLLER] nothing to do...");
		lastTime = System.currentTimeMillis();
	}


	/**
	 * Try increase the system performance
	 * @param alarm
	 * @param monitor
	 * @param executor
	 */
	private synchronized void minPerformanceHandler(Alarm alarm, MonitorController monitor, ExecutorController executor) {

		if ( alarm != Alarm.VIOLATION ) return;
		
		if (System.currentTimeMillis() - lastTime < delay) {
			return;
		}

		System.out.println("[PLANNER_CONTROLLER] trying to add a worker...");
		try {
			ObjectWrapper result = executor.execute("improve-solvers($this, " + maxWorkers + ");");
			if (result.isValid() && (boolean) result.getObject()) {
				System.out.println("[PLANNER_CONTROLLER] new worker added.");
				lastTime = System.currentTimeMillis();
				return;
			}
		} catch (WrongValueException e2) {
			e2.printStackTrace();
		}

		try {
			ObjectWrapper result = executor.execute("value($this/child::CrackerManager/attribute::numberOfSolvers);");
			if (result.isValid() && maxSolvers > (double) result.getObject()) {
				System.out.println("[PLANNER_CONTROLLER] trying to add new solver...");
				try {
					result = executor.executeAction(AddSolverAction.DEFAULT_NAME);
					if (result.isValid() && (boolean) result.getObject()) {
						System.out.println("[PLANNER_CONTROLLER] new solver added.");
						lastTime = System.currentTimeMillis();
						return;
					}
				} catch (WrongValueException e1) {
					e1.printStackTrace();
				}
			}
		} catch (WrongValueException e1) {
			e1.printStackTrace();
		}

		// Try to add a new solver
		

		System.out.println("[PLANNER_CONTROLLER] nothing to do...");
		lastTime = System.currentTimeMillis();
	}

}
