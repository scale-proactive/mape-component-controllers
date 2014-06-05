package examples.md5cracker.cracker.solver;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.*;

public interface WorkerMulticast {

	public static final String ITF_NAME = "worker-multicast-itf";

	@MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN))
	List<Wrapper<String>> solve(List<Instruction> inst);

}
