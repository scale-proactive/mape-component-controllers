package examples.md5cracker.cracker;

import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


@DefineGroups({ @Group(name = "G1", selfCompatible = true) })
public interface Cracker {
	
	@MemberOf("G1")
	public StringWrapper crack(byte[] hash, int maxLength);

}
