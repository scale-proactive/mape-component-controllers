package tests.components;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

public class SlaveImpl implements Slave, RunActive {

	@Override
	public void run1() {
		System.out.println("run1 working");
	}

	@Override
	public void run2() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("run2 working");
	}

	@Override
	public void runActivity(Body body) {
		(new ComponentMultiActiveService(body)).multiActiveServing();
	}

}
