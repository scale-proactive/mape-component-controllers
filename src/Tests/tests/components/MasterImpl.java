package tests.components;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

public class MasterImpl implements Master, BindingController, RunActive {

	private Slave slave;

	@Override
	public void run() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		slave.run1();
	}
	
	@Override
	public void run2() {
		slave.run2();
	}

	@Override
	public void bindFc(String arg0, Object arg1) throws NoSuchInterfaceException {
		if (arg0.equals("slave")) {
			slave = (Slave) arg1;
		} else {
			throw new NoSuchInterfaceException(arg0);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {"slave"};
	}

	@Override
	public Object lookupFc(String arg0) throws NoSuchInterfaceException {
		if (arg0.equals("slave")) {
			return slave;
		} else {
			throw new NoSuchInterfaceException(arg0);
		}
	}

	@Override
	public void unbindFc(String arg0) throws NoSuchInterfaceException  {
		if (arg0.equals("slave")) {
			slave = null;
		} else {
			throw new NoSuchInterfaceException(arg0);
		}
	}

	@Override
	public void runActivity(Body body) {
		(new ComponentMultiActiveService(body)).multiActiveServing();
	}

}
