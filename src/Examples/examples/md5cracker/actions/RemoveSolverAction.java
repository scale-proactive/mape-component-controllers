package examples.md5cracker.actions;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAMulticastController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extra.component.mape.execution.Action;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;

import examples.md5cracker.cracker.Cracker;
import examples.md5cracker.cracker.CrackerAttributes;
import examples.md5cracker.cracker.SolverMulticast;
import examples.md5cracker.cracker.solver.ResultRepository;
import examples.md5cracker.cracker.solver.Solver;
import examples.md5cracker.cracker.solver.SolverAttributes;
import examples.md5cracker.cracker.solver.SolverManager;
import examples.md5cracker.cracker.solver.TaskRepository;
import examples.md5cracker.cracker.solver.Worker;
import examples.md5cracker.cracker.solver.WorkerMulticast;


public class RemoveSolverAction extends Action {

	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_NAME = "remove-solver-action";
	
	@Override
	public Object execute(Component component, PAGCMTypeFactory typeFactory, PAGenericFactory genericFactory) {
		try {
			
			// Get the appropriate solver reference
			Component solver = null;
			Component solverManager = null;
			double id = -1;
			for (Component comp : this.getSubComponent(component, "Solver")) {
				Component compManager = this.getBindComponent(comp, Solver.ITF_NAME);
				double solverId = ((SolverAttributes) GCM.getAttributeController(compManager)).getId();
				if (solverId > id) {
					id = solverId;
					solver = comp;
					solverManager = compManager;
				}
			}
			
			if (solver == null) {
				return false;
			}
			
			// Unbind this solver from crackerManager

			
			Component crackerManager = this.getBindComponent(component, Cracker.ITF_NAME);
			Utils.getPAGCMLifeCycleController(crackerManager).stopFc();
			Utils.getPAMulticastController(crackerManager).unbindGCMMulticast(SolverMulticast.ITF_NAME,
					solver.getFcInterface(Solver.ITF_NAME));
			Utils.getPAGCMLifeCycleController(crackerManager).startFc();
			Remmos.enableMonitoring(crackerManager);

			Utils.getPAGCMLifeCycleController(solverManager).stopFc();
			
			Component[] workers = this.getSubComponent(solver, "Worker");
			for (Component worker : workers) {
				Utils.getPAGCMLifeCycleController(worker).stopFc();
			}
		
			Utils.getPAGCMLifeCycleController(solver).stopFc();
			BindingController bindingController = Utils.getPABindingController(solver);
			bindingController.unbindFc(TaskRepository.ITF_NAME);
			bindingController.unbindFc(ResultRepository.ITF_NAME);
			bindingController.unbindFc(Solver.ITF_NAME);
			
			bindingController = Utils.getPABindingController(solverManager);
			bindingController.unbindFc(SolverManager.CLIENT_ITF_NAME);
			bindingController.unbindFc(TaskRepository.ITF_NAME);
			bindingController.unbindFc(ResultRepository.ITF_NAME);
			
			PAMulticastController multicastController = Utils.getPAMulticastController(solverManager);
			for (Component worker : workers) {
				multicastController.unbindGCMMulticast(WorkerMulticast.ITF_NAME,
						worker.getFcInterface(Worker.ITF_NAME));
				
				//Utils.getPAGCMLifeCycleController(worker).terminateGCMComponent();
			}
			
			//Utils.getPAGCMLifeCycleController(solverManager).terminateGCMComponent();
			//Utils.getPAGCMLifeCycleController(solver).terminateGCMComponent();
			
			try {
				Utils.getPAGCMLifeCycleController(component).stopFc();
				Utils.getPAContentController(component).removeFcSubComponent(solver);
				Utils.getPAGCMLifeCycleController(component).startFc();
			} catch (IllegalContentException e) {
				e.printStackTrace();
			}
			
			
			CrackerAttributes crackerAttributes = (CrackerAttributes) GCM.getAttributeController(crackerManager);
			crackerAttributes.setNumberOfSolvers(crackerAttributes.getNumberOfSolvers() - 1);
			return true;
			
		} catch (NoSuchInterfaceException | IllegalLifeCycleException | IllegalBindingException e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
