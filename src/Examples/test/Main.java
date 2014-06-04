package test;

import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.control.PAMembraneController;


public class Main {

	public static void main(String[] args) throws Exception {
		boolean api = true;
		if (api) APITest.main(args);
		else run_with_adl();
	}

	public static void run_with_adl() throws Exception  {
		Factory f = FactoryFactory.getFactory();
		Map<Object, Object> context = new HashMap<Object, Object>();
        String gcmADL = "test.Composite";
        Component comp = (Component) f.newComponent(gcmADL, context);
        
        //checkMembranes(comp);
        
        Utils.getPAGCMLifeCycleController(comp).startFc();

        Master master = (Master) comp.getFcInterface("test");
        master.run();
        master.run2();
        Monitor mon = (Monitor) comp.getFcInterface("monitor-controller");
        System.out.println(mon.getMonitoring());
        Thread.sleep(1000);
        System.out.println(mon.getMonitoring());
        Thread.sleep(1000);
        System.out.println(mon.getMonitoring());
        Thread.sleep(1000);
        System.out.println(mon.getMonitoring());
        Thread.sleep(1000);
        System.out.println(mon.getMonitoring());
        Thread.sleep(1000);
        System.out.println(mon.getMonitoring());
	}
	
    /** 
     * DFS search looking for membranes in each component beginning from 'start'
     * @param start
     */
    public static void checkMembranes(Component start) {

        PAMembraneController pamc = null;
        String compName = null;

        try {
            compName = GCM.getNameController(start).getFcName();
            pamc = Utils.getPAMembraneController(start);
        } catch (NoSuchInterfaceException e1) {
            pamc = null;
        }
        System.out.println("Component " + compName + " has MembraneController? " + (pamc != null));

        try {
            Component[] subComponents = GCM.getContentController(start).getFcSubComponents();
            for (Component comp : subComponents) {
                checkMembranes(comp);
            }
        } catch (NoSuchInterfaceException e) {
            // primitive component ... silently continue
        }
    }
}
