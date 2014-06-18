package test;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.Action;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

public class APITest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Component boot = Utils.getBootstrapComponent();
		PAGCMTypeFactory tf = Utils.getPAGCMTypeFactory(boot);
		PAGenericFactory cf = Utils.getPAGenericFactory(boot);

		Active active = new ComponentRunActive(){
			@Override
			public void runComponentActivity(Body body) {
				(new ComponentMultiActiveService(body)).multiActiveServing();
			}};

	
		// MASTER

		PAGCMInterfaceType[] func = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType("test", Master.class.getName(), PAGCMTypeFactory.SERVER, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
			(PAGCMInterfaceType) tf.createGCMItfType("slave", Slave.class.getName(), PAGCMTypeFactory.CLIENT, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY) };
		
		final Component master = cf.newFcInstance(
				tf.createFcType(func, new PAGCMInterfaceType[] {}),
				new ControllerDescription("Master", Constants.PRIMITIVE),
				new ContentDescription(MasterImpl.class.getName(), null, active, null),
				null);
		
		
		//SLAVE 
		
		func = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType(
				"slave", Slave.class.getName(),
				PAGCMTypeFactory.SERVER,
				PAGCMTypeFactory.MANDATORY,
				PAGCMTypeFactory.SINGLETON_CARDINALITY)};
		
		final Component slave2 = cf.newFcInstance(
				tf.createFcType(func, new PAGCMInterfaceType[] {}),
				new ControllerDescription("Slave2", Constants.PRIMITIVE),
				new ContentDescription(SlaveImpl.class.getName(), null, active, null),
				null);
		
		final Component slave1 = cf.newFcInstance(
				tf.createFcType(func, new PAGCMInterfaceType[] {}),
				new ControllerDescription("Slave1", Constants.PRIMITIVE),
				new ContentDescription(SlaveImpl.class.getName(), null, active, null),
				null);

		// COMPOSITE

		func = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("test", Master.class.getName(), PAGCMTypeFactory.SERVER, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
		};
		
		final Component composite = cf.newFcInstance(
				tf.createFcType(func, Remmos.createMonitorableNFType(tf, func, Constants.COMPOSITE)),
				new ControllerDescription("Composite", Constants.COMPOSITE),
				new ContentDescription(Composite.class.getName(), null, active, null),
				null);
		
		GCM.getContentController(composite).addFcSubComponent(master);
		GCM.getContentController(composite).addFcSubComponent(slave1);
		GCM.getBindingController(composite).bindFc("test", master.getFcInterface("test"));
		GCM.getBindingController(master).bindFc("slave", slave1.getFcInterface("slave"));
		
		System.out.println("ASDASDASD");
		Remmos.addObjectControllers((PAComponent) composite);
		Utils.getPAMembraneController(composite).startMembrane();
		Remmos.addExecutorController(composite);
	
		Utils.getPAGCMLifeCycleController(composite).startFc();
		
		final Master test = (Master) composite.getFcInterface("test");

		Remmos.getExecutorController(composite).execute("1+1");
		
		Action action = new Action() {
			private static final long serialVersionUID = 1L;
			@Override
			public Object execute(Component component, PAGCMTypeFactory tf, PAGenericFactory cf) {
				try {
					Utils.getPAGCMLifeCycleController(component).stopFc();
					Utils.getPAContentController(composite).addFcSubComponent(slave2);
					GCM.getBindingController(master).unbindFc("slave");
					
					System.out.println("[REC] bind slave2");
					GCM.getBindingController(master).bindFc("slave", slave2.getFcInterface("slave"));
					
					System.out.println("[REC] start again");
					GCM.getLifeCycleController(composite).startFc();
					System.out.println("[REC] done");
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		};
		
		Remmos.getExecutorController(composite).executeAction(action);
	
		test.run();
		Thread.sleep(1000);
	
		test.run2();
		Thread.sleep(1000);
		
		test.run();
		test.run2();
		
		Thread.sleep(1000);
		System.out.println("COMPOSITE name = " + GCM.getNameController(composite).getFcName());
		System.out.println("MASTER name = " + GCM.getNameController(master).getFcName());
		System.out.println("SLAVE name = " + GCM.getNameController(slave2).getFcName());


	
		//Utils.getPAGCMLifeCycleController(comp).startFc();
	}

}
