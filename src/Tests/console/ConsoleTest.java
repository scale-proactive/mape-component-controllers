package console;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.extensions.autonomic.adl.AFactory;
import org.objectweb.proactive.extensions.autonomic.adl.AFactoryFactory;
import org.objectweb.proactive.extensions.autonomic.console.Console;

import tests.components.Master;

public class ConsoleTest {
	
	protected static AFactory adlFactory;
	    
	static public void main(String[] args) throws Exception {

    	System.setProperty("gcm.provider", "org.objectweb.proactive.core.component.Fractive");
       	adlFactory = (AFactory) AFactoryFactory.getAFactory();
       	final Component composite = (Component) adlFactory.newAutonomicComponent("tests.components.Composite", null);

       	(new Thread(new Runnable() {

			@Override
			public void run() {
				Master master = null;
				try {
					master = (Master) composite.getFcInterface("test-itf");
				} catch (NoSuchInterfaceException e) {
					e.printStackTrace();
					return;
				}
				
				while (true) {
					try {
						master.run();
						Thread.sleep(100);
						
						master.run2();
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
			}
       		
       	})).start();
 
       	Console console = new Console(composite);
       	console.run();
	}

}
