package examples.md5cracker.actions;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.control.PAMulticastController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extra.component.mape.execution.Action;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;

import examples.md5cracker.cracker.CCST;
import examples.md5cracker.cracker.solver.SolverAttributes;


public class RemoveSolverAction extends Action {

	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_NAME = "remove-solver-action";
	
	@Override
	public Object execute(Component cracker, PAGCMTypeFactory typeFactory, PAGenericFactory genericFactory) {

	
		Component crackerManager;
		try {
			crackerManager = this.getBindComponent(cracker, CCST.CRACKER_ITF);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			return false;
		}
	

		Component solver;
		Component solverManager;
		try {
			solver = this.getBindComponent(crackerManager, CCST.SOLVER_C3);
			if (solver != null) {
				solverManager = this.getBindComponent(solver, CCST.SOLVER);
				if (solverManager != null) {
					if ( 1 >= ((SolverAttributes) GCM.getAttributeController(solverManager)).getNumberOfWorkers() ) {
						return removeSolver(3, CCST.SOLVER_C3, solver, solverManager, cracker);
					}
				}
			}
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		try {
			solver = this.getBindComponent(crackerManager, CCST.SOLVER_C2);
			if (solver != null) {
				solverManager = this.getBindComponent(solver, CCST.SOLVER);
				if (solverManager != null) {
					if ( 1 >= ((SolverAttributes) GCM.getAttributeController(solverManager)).getNumberOfWorkers() ) {
						return removeSolver(2, CCST.SOLVER_C2, solver, solverManager, cracker);
					}
				}
			}
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		
		try {
			solver = this.getBindComponent(crackerManager, CCST.SOLVER_C1);
			if (solver != null) {
				solverManager = this.getBindComponent(solver, CCST.SOLVER);
				if (solverManager != null) {
					if ( 1 >= ((SolverAttributes) GCM.getAttributeController(solverManager)).getNumberOfWorkers() ) {
						return removeSolver(1, CCST.SOLVER_C1, solver, solverManager, cracker);
					}
				}
			}
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
	
		return false;
	}

	private Object removeSolver(int i, String clientItfName, Component solver, Component solverManager, Component cracker) {
		
		try {
			Component crackerManager = this.getBindComponent(cracker, CCST.CRACKER_ITF);
			
			// Remove from cracker
			Utils.getPAGCMLifeCycleController(cracker).stopFc();
			
			Remmos.unbind(crackerManager.getFcInterface(clientItfName));
			Utils.getPABindingController(crackerManager).unbindFc(clientItfName);
			Utils.getPAContentController(cracker).removeFcSubComponent(solver);

			Remmos.enableMonitoring(crackerManager);
			Utils.getPAGCMLifeCycleController(cracker).startFc();

			// delete components
			Utils.getPABindingController(solver).unbindFc(CCST.SOLVER);
			
			
			PAMulticastController mc = Utils.getPAMulticastController(solverManager);
			for (Object workerItf : mc.lookupGCMMulticast(CCST.WORKER_MULTICAST)) {
				mc.unbindGCMMulticast(CCST.WORKER_MULTICAST, workerItf);
			}
			
			PAContentController cc = Utils.getPAContentController(solver);
			for (Component subComp : cc.getFcSubComponents()) {
				cc.removeFcSubComponent(subComp);
				//Utils.getPAGCMLifeCycleController(subComp).terminateGCMComponent();
			}
			
			//Utils.getPAGCMLifeCycleController(solver).terminateGCMComponent();
			

	
			return true;
	
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
