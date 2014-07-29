package cl.niclabs.autonomic.examples.balancer.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;


public interface WorkerItf {

	public Wrapper<String> workOn(Task task);

}
