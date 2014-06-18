package examples.services.autoadaptable;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import examples.services.Service;
import examples.services.autoadaptable.components.ManagerAttributes;
import examples.services.autoadaptable.components.ManagerImpl;
import examples.services.autoadaptable.components.MasterAttributes;
import examples.services.autoadaptable.components.MasterImpl;
import examples.services.autoadaptable.components.Slave;
import examples.services.autoadaptable.components.SlaveImpl;
import examples.services.autoadaptable.components.SlaveMulticast;
import examples.services.autoadaptable.components.Solver;


public class AASFactory {

	private static String SC = PAGCMTypeFactory.SINGLETON_CARDINALITY;
	private static String MC = PAGCMTypeFactory.MULTICAST_CARDINALITY;
	private static boolean MND = PAGCMTypeFactory.MANDATORY;
	private static boolean CLI = PAGCMTypeFactory.CLIENT;
	private static boolean SRV = PAGCMTypeFactory.SERVER;

	private static Remmos remmos;
	
	private static void checkRemmos(PAGCMTypeFactory tf, PAGenericFactory cf) throws InstantiationException {
		if (remmos == null) {
			remmos = new Remmos(tf, cf);
		}
	}

	/** Service composite **/
	public static Component createService(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
	
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType(AASCST.SERVICE, Service.class.getName(), SRV, MND, SC),
		};
	
		checkRemmos(tf, cf);

		Component comp = remmos.newFcInstance(
				remmos.createFcType(fTypes, Constants.COMPOSITE),
				new ControllerDescription(AASCST.SERVICE_COMP_NAME, Constants.COMPOSITE),
				null, node);

		Utils.getPAMembraneController(comp).startMembrane();
		Remmos.addMonitoring(comp);
		Remmos.addAnalysis(comp);
		Remmos.addPlannerController(comp);
		Remmos.addExecutorController(comp);
		return comp;
	}

	/** Manager primitive **/
	public static Component createManager(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType(AASCST.MANAGER, Service.class.getName(), SRV, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(Constants.ATTRIBUTE_CONTROLLER, ManagerAttributes.class.getName(), SRV, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(AASCST.SOLVER_C1, Solver.class.getName(), CLI, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(AASCST.SOLVER_C2, Solver.class.getName(), CLI, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(AASCST.SOLVER_C3, Solver.class.getName(), CLI, MND, SC),
		};
	
		checkRemmos(tf, cf);

		Component comp = remmos.newFcInstance(
				remmos.createFcType(fTypes, Constants.PRIMITIVE),
				new ControllerDescription(AASCST.MANAGER_COMP_NAME, Constants.PRIMITIVE),
				new ContentDescription(ManagerImpl.class.getName()),
				node);

		Utils.getPAMembraneController(comp).startMembrane();
		Remmos.addMonitoring(comp);
		return comp;
	}


	/** Solver composite **/
	public static Component createSolver(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {

		PAGCMInterfaceType[] fTypes  = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType(AASCST.SOLVER, Solver.class.getName(), SRV, MND, SC),
		};	

		checkRemmos(tf, cf);

		Component comp = remmos.newFcInstance(
				remmos.createFcType(fTypes, Constants.COMPOSITE),
				new ControllerDescription(AASCST.SOLVER_COMP_NAME, Constants.COMPOSITE),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
						@Override
						public void runComponentActivity(Body body) {
							(new ComponentMultiActiveService(body)).multiActiveServing();
					}}, null),
				node);

		Utils.getPAMembraneController(comp).startMembrane();
		Remmos.addMonitoring(comp);
		Remmos.addExecutorController(comp);

		return comp;
	}

	public static Component createMaster(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType(AASCST.MASTER, Solver.class.getName(), SRV, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(Constants.ATTRIBUTE_CONTROLLER, MasterAttributes.class.getName(), SRV, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(AASCST.SLAVE_MULTICAST, SlaveMulticast.class.getName(), CLI, MND, MC),
		};

		return cf.newFcInstance(
				tf.createFcType(fTypes),
				new ControllerDescription(AASCST.MASTER_COMP_NAME, Constants.PRIMITIVE),
				new ContentDescription(MasterImpl.class.getName()),
				node);
	}

	public static Component createSlave(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType(AASCST.SLAVE, Slave.class.getName(), SRV, MND, SC),
		};

		return cf.newFcInstance(
				tf.createFcType(fTypes),
				new ControllerDescription(AASCST.SLAVE_COMP_NAME, Constants.PRIMITIVE),
				new ContentDescription(SlaveImpl.class.getName()),
				node);
	}

	public static void bindSolver(Component solver, Component master, Component[] slaves) throws Exception {
	
		PAContentController cc = Utils.getPAContentController(solver);
		
		cc.addFcSubComponent(master);
		Utils.getPABindingController(solver).bindFc(AASCST.SOLVER, master.getFcInterface(AASCST.MASTER));
		
		PABindingController bc = Utils.getPABindingController(master);

		for (Component slave : slaves) {
			cc.addFcSubComponent(slave);
			bc.bindFc(AASCST.SLAVE_MULTICAST, slave.getFcInterface(AASCST.SLAVE));
		}
	}

	public static void bindService(Component service, Component manager, Component[] solvers) throws Exception {
		
		PAContentController cc = Utils.getPAContentController(service);
		
		cc.addFcSubComponent(manager);
		Utils.getPABindingController(service).bindFc(AASCST.SERVICE, manager.getFcInterface(AASCST.MANAGER));
		
		PABindingController bc = Utils.getPABindingController(manager);
		
		cc.addFcSubComponent(solvers[0]);
		bc.bindFc(AASCST.SOLVER_C1, solvers[0].getFcInterface(AASCST.SOLVER));
		
		cc.addFcSubComponent(solvers[1]);
		bc.bindFc(AASCST.SOLVER_C2, solvers[1].getFcInterface(AASCST.SOLVER));

		cc.addFcSubComponent(solvers[2]);
		bc.bindFc(AASCST.SOLVER_C3, solvers[2].getFcInterface(AASCST.SOLVER));
	}

	public static Component createCompleteSolver(int slavesNumber, Node node, PAGCMTypeFactory patf,
			PAGenericFactory pagf) throws Exception {
	
		Component solver = AASFactory.createSolver(node, patf, pagf);
		Component master = AASFactory.createMaster(node, patf, pagf);
		Component[] slaves = new Component[slavesNumber];
	
		for (int i = 0; i < slavesNumber; i++)
			slaves[i] = AASFactory.createSlave(node, patf, pagf);
	
		AASFactory.bindSolver(solver, master, slaves);
		((MasterAttributes) GCM.getAttributeController(master)).setSlavesNumber(slavesNumber);
	
		return solver;
	}

}
