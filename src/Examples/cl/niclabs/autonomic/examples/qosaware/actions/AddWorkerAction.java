package cl.niclabs.autonomic.examples.qosaware.actions;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.autonomic.adl.implementations.AComponentRunActive;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.Action;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import cl.niclabs.autonomic.examples.qosaware.components.JonasDispatcherAttr;
import cl.niclabs.autonomic.examples.qosaware.components.JonasImpl;
import cl.niclabs.autonomic.examples.qosaware.components.JonasItf;
import cl.niclabs.autonomic.examples.qosaware.components.JonasRetItf;

public class AddWorkerAction extends Action {

	private static final long serialVersionUID = 1L;
	private String dispatcherName;
	private Node node;

	public AddWorkerAction(String dispatcherName, Node node) {
		this.dispatcherName = dispatcherName;
		this.node = node;
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
			PAGCMInterfaceType[] workerItfTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) typeFactory.createGCMItfType("jeeReq", JonasItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) typeFactory.createGCMItfType("jeeReturn", JonasRetItf.class.getName(), true, false, "singleton")};
//					(PAGCMInterfaceType) typeFactory.createGCMItfType("worker", WorkerItf.class.getName(),
//							false, false, "singleton"),
//				};

			Remmos remmos = new Remmos(typeFactory, genericFactory);
			worker = remmos.newFcInstance(
					remmos.createFcType(workerItfTypes, "primitive"),
					new ControllerDescription("Jonas2", "primitive"),
					new ContentDescription(JonasImpl.class.getName(), null, new AComponentRunActive() {
						private static final long serialVersionUID = 1L;
						public void runComponentActivity(Body body) {
							(new ComponentMultiActiveService(body)).multiActiveServing();
						}
					}, null),
					node);

			Utils.getPAMembraneController(worker).startMembrane();

		} catch (InstantiationException | IllegalLifeCycleException | NoSuchInterfaceException e1) {
			e1.printStackTrace();
		}
		
		if (worker == null) {
			return "Worker creation fail.";
		}
		
		try {
			PAGCMLifeCycleController lifeCycleController = Utils.getPAGCMLifeCycleController(solver);
			lifeCycleController.stopFc();
			
			Utils.getPAContentController(solver).addFcSubComponent(worker);
			//Utils.getPABindingController(dispatcher).bindFc("worker-multicast", worker.getFcInterface("worker"));
			Utils.getPABindingController(dispatcher).bindFc("jeeReqm", worker.getFcInterface("jeeReq"));
			Utils.getPABindingController(worker).bindFc("jeeReq", solver.getFcInterface("jeeReq"));
			
			JonasDispatcherAttr attr = (JonasDispatcherAttr) GCM.getAttributeController(dispatcher);
			attr.setWorkers(attr.getWorkers() + 1);	
			
			lifeCycleController.startFc();

		} catch (NoSuchInterfaceException | IllegalLifeCycleException | IllegalContentException | IllegalBindingException e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}

		return true;
	}

}
