package cl.niclabs.autonomic.examples.balancer.components;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface WorkerMulticastItf {

	@MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN))
	public List<Wrapper<String>> workOn(List<Task> tasks);

}
