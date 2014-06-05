package org.objectweb.proactive.extra.component.mape.reconfiguration;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

public abstract class Action implements Serializable, Executable {

	private static final long serialVersionUID = 1L;

	/**
	 * Returns the component bound to this "itfName" interface. If this is a multicast interface, it will
	 * try to return one of the, possible many, bound components. If something goes wrong, a null value
	 * will be returned.
	 * 
	 * @param component The client component
	 * @param itfName The name the interface
	 * @return A bound component, or null if it fails
	 */
	public Component getBindComponent(Component component, String itfName) {
		try {
			PAInterface itf = (PAInterface) Utils.getPABindingController(component).lookupFc(itfName);
			if (((PAGCMInterfaceType) itf.getFcItfType()).isGCMMulticastItf()) {
				return getMulticastBindComponenents(component, itfName)[0];
			}
			return itf.getFcItfOwner();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the array of components bound the "multicastItfName" multicast interface, or null if it fails
	 * to getting them.
	 * 
	 * @param component	The client component
	 * @param multicastItfName The name of the multicast interface
	 * @return Array of bound components, or null if it fails
	 */
	public Component[] getMulticastBindComponenents(Component component, String multicastItfName) {
		try {
			Object[] destinationItfs = Utils.getPAMulticastController(component).lookupGCMMulticast(multicastItfName);
			Component[] destinationComps = new Component[destinationItfs.length];
			for (int i = 0; i < destinationItfs.length; i++) {
				destinationComps[i] = ((PAInterface) destinationItfs[i]).getFcItfOwner();
			}
			return destinationComps;
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			return null;
		}
	}

	public abstract Object execute(Component component, PAGCMTypeFactory tf, PAGenericFactory cf);

}
