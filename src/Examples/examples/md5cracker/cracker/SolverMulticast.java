package examples.md5cracker.cracker;

import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;


public interface SolverMulticast {

	public static final String ITF_NAME = "solver-multicast-itf";
	
	@MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST))
	public void start(String alphabet, int maxLength);
	
}
