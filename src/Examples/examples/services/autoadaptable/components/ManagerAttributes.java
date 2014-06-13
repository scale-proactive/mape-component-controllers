package examples.services.autoadaptable.components;

import org.objectweb.fractal.api.control.AttributeController;

public interface ManagerAttributes extends AttributeController {

	public String getPoints();
	public void setPoints(String points);

}
