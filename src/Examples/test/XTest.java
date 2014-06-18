package test;

import java.util.HashMap;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.extensions.autonomic.adl.AFactoryFactory;

public class XTest {

	public static void main(String[] args) throws Exception {
		System.setProperty("gcm.provider", "org.objectweb.proactive.core.component.Fractive");
		Factory factory =  AFactoryFactory.getAFactory();
		Component comp = (Component) factory.newComponent("test.Composite", new HashMap<String, Object>());
		
		PAComponent pacomp = (PAComponent) comp;
		System.out.println("----- " + pacomp.getComponentParameters().getName());
		for (Object itf : pacomp.getFcInterfaces()) {
			if (itf instanceof PAInterface) {
				System.out.println(((PAInterface) itf).getFcItfName() + ":" );
			}
		}

		for (Component subComp : Utils.getPAContentController(comp).getFcSubComponents()) {
			PAComponent subPAComp = (PAComponent) subComp;
			System.out.println("----- " + subPAComp.getComponentParameters().getName());
			for (Object itf : subComp.getFcInterfaces()) {
				if (itf instanceof PAInterface) {
					System.out.println(((PAInterface) itf).getFcItfName() + ":" );
				}
			}
		}
	}

}
