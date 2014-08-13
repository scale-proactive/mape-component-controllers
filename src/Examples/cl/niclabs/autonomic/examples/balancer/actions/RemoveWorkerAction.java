package cl.niclabs.autonomic.examples.balancer.actions;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.Action;

import cl.niclabs.autonomic.examples.balancer.components.DispatcherAttr;

public class RemoveWorkerAction extends Action {

	private static final long serialVersionUID = 1L;
	private String dispatcherName;

	public RemoveWorkerAction(String dispatcherName) {
		this.dispatcherName = dispatcherName;
	}

	@Override
	public Object execute(Component solver, PAGCMTypeFactory typeFactory, PAGenericFactory genericFactory) {

		Component dispatcher = null;
		try {
			Component[] subComps = Action.getSubComponent(solver, dispatcherName);
			if (subComps.length > 0) {
				dispatcher = subComps[0];
			}
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		if (dispatcher == null) {
			return "Add worker failed. Dispatcher with name \"" + dispatcherName + "\" not found.";
		}

		Component worker = null;
		try {			
			Component[] subComps = Action.getSubComponent(solver, "Worker");
			if (subComps.length > 0) {
				worker = subComps[subComps.length - 1];
			}
		} catch (NoSuchInterfaceException e1) {
			e1.printStackTrace();
		}
		
		if (worker == null) {
			return "Worker look up fail.";
		}
		
		try {
			PAGCMLifeCycleController lifeCycleController = Utils.getPAGCMLifeCycleController(solver);
			lifeCycleController.stopFc();
			
			Utils.getPAMulticastController(dispatcher).unbindGCMMulticast("worker-multicast", worker.getFcInterface("worker"));			
			Utils.getPAContentController(solver).removeFcSubComponent(worker);

			DispatcherAttr attr = (DispatcherAttr) GCM.getAttributeController(dispatcher);
			attr.setWorkers(attr.getWorkers() - 1);	
			
			lifeCycleController.startFc();

		} catch (NoSuchInterfaceException | IllegalLifeCycleException | IllegalContentException | IllegalBindingException e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}

		return true;
	}

}
