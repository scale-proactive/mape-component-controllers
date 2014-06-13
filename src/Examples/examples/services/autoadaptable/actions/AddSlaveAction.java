package examples.services.autoadaptable.actions;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.component.mape.execution.Action;

import examples.services.autoadaptable.AASCST;
import examples.services.autoadaptable.AASFactory;
import examples.services.autoadaptable.components.MasterAttributes;

public class AddSlaveAction extends Action {

	private static final long serialVersionUID = 1L;
	
	private Node node;

	
	public AddSlaveAction(Node node) {
		this.node = node;
	}

	@Override
	public Object execute(Component solver, PAGCMTypeFactory typeFactory, PAGenericFactory genericFactory) {
		
		// NOTE: assumed to be executed on Solver composite component
		
		try {
			Component slave = AASFactory.createSlave(node, typeFactory, genericFactory);
			Component master = Action.getBindComponent(solver, AASCST.SOLVER);

			System.out.println("Stopping solver");
			PAGCMLifeCycleController lc = Utils.getPAGCMLifeCycleController(solver);
			lc.stopFc();
			
			System.out.println("add slave to solver");
			Utils.getPAContentController(solver).addFcSubComponent(slave);
			System.out.println("bind slave to master");
			Utils.getPABindingController(master).bindFc(AASCST.SLAVE_MULTICAST, slave.getFcInterface(AASCST.SLAVE));
			System.out.println("setting the attributes");
			MasterAttributes masterAttr = (MasterAttributes) GCM.getAttributeController(master);
			
			long slavesNumber = (long) (masterAttr.getSlavesNumber() + 1);
			masterAttr.setSlavesNumber(slavesNumber);
			System.out.println("Starting solver " + ((PAComponent) solver).getComponentParameters().getControllerDescription().getName());
			lc.startFc();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
