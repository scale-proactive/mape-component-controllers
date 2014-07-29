package cl.niclabs.autonomic.examples.balancer.components;

import org.objectweb.fractal.api.control.AttributeController;


public interface BalancerAttr extends AttributeController {

	/** Get points with format p1 + "u" + p2 */
	public String getPoints();

	/** Set points using the format p1 + "u" + p2, for example: "0.4u0.6" */
	public void setPoints(String points);

}
