package test;

public class MonitorImpl implements Monitor {

	private int n = 0;

	@Override
	public String getMonitoring() {
		n += 1;
		return "monitoring stuff " + n;
	}

}
