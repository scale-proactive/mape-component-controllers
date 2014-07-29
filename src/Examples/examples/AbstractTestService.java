package examples;

import java.io.File;
import java.net.URL;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

public abstract class AbstractTestService implements Runnable {

	PAGCMTypeFactory patf;
	PAGenericFactory pagf;
	GCMApplication gcma;

	AbstractTestService(String appDescriptor) throws Exception {
		Component boot = Utils.getBootstrapComponent();
		patf = Utils.getPAGCMTypeFactory(boot);
		pagf = Utils.getPAGenericFactory(boot);
		
		if (appDescriptor != null) {
			File file = new File((new URL(appDescriptor)).toURI().getPath());
			
			gcma = PAGCMDeployment.loadApplicationDescriptor(file);
			gcma.startDeployment();
			gcma.waitReady();
		}
	}

}
