package examples.services.autoadaptable.actions;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.component.mape.execution.Action;

import examples.services.autoadaptable.AASCST;
import examples.services.autoadaptable.AASFactory;
import examples.services.autoadaptable.components.MasterAttributes;

public class RemoveSlaveAction extends Action {

	private static final long serialVersionUID = 1L;

	private Node node;

	
	public RemoveSlaveAction(Node node) {
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
			
			PAContentController cc = Utils.getPAContentController(solver);
			for (Component subComp : cc.getFcSubComponents()) {
				if (((PAComponent) subComp).getComponentParameters().getName().equals(AASCST.SLAVE_COMP_NAME)) {
					Utils.getPAMulticastController(master).unbindGCMMulticast(AASCST.SLAVE_MULTICAST,
							subComp.getFcInterface(AASCST.SLAVE));
					cc.removeFcSubComponent(subComp);
					break;
				}
			}
			System.out.println("setting the attributes");
			MasterAttributes masterAttr = (MasterAttributes) GCM.getAttributeController(master);
			
			long slavesNumber = (long) (masterAttr.getSlavesNumber() + -1);
			masterAttr.setSlavesNumber(slavesNumber);
			lc.startFc();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	
}
