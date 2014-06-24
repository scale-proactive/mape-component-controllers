package tests.components;


public class SlaveImpl implements Slave {

	@Override
	public long run1() {
		return System.currentTimeMillis();
	}

	@Override
	public long run2() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return System.currentTimeMillis();
		//System.out.println("run2 working");
	}

}
