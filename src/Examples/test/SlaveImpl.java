package test;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

@DefineGroups({ @Group(name = "G1", selfCompatible = true) })
public class SlaveImpl implements Slave, RunActive {

	@Override
	@MemberOf("G1")
	public void run1() {
		System.out.println("run1 working");
	}

	@Override
	@MemberOf("G1")
	public void run2() {
		for(int i = 0; i <= 5; i++) {
			System.out.println("run2 working");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	@Override
	public void runActivity(Body body) {
		(new ComponentMultiActiveService(body)).multiActiveServing();
	}

}
