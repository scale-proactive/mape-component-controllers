package tests.components;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;

public class MasterImpl implements Master, BindingController {

	private Slave slave;

	@Override
	public long run() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return slave.run1();
	}
	
	@Override
	public long run2() {
		return slave.run2();
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

}
