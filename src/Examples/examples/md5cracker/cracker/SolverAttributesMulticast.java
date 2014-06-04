package examples.md5cracker.cracker;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.ClassDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;

@ClassDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST))
public interface SolverAttributesMulticast {

	public static final String ITF_NAME = "solver-attributes-multicast-itf";
	
	public void setNumberOfWorkers(int number);
	public List<Integer> getNumberOfWorkers();
	
}
