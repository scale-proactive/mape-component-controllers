package org.objectweb.proactive.extra.component.mape.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

public abstract class Action implements Serializable {

	private static final long serialVersionUID = 1L;

	public static Component[] getSubComponent(Component component, String subComponentName) throws NoSuchInterfaceException {
		List<Component> list = new ArrayList<Component>();
		for (Component subComp : Utils.getPAContentController(component).getFcSubComponents()) {
			String subCompName = ((PAComponent) subComp).getComponentParameters().getControllerDescription().getName();
			if (subCompName.equals(subComponentName)) {
				list.add(subComp);
			}
		}
		return list.toArray(new Component[list.size()]);
	}

	/**
	 * Returns the component bound to this "itfName" interface. If this is a multicast interface, it will
	 * try to return one of the, possible many, bound components. If something goes wrong, a null value
	 * will be returned.
	 * 
	 * @param component The client component
	 * @param itfName The name the interface
	 * @return A bound component, or null if no server component is bound to this interface
	 * @throws NoSuchInterfaceException 
	 */
	public static Component getBindComponent(Component component, String itfName) throws NoSuchInterfaceException {

		PAInterface itf = (PAInterface) Utils.getPABindingController(component).lookupFc(itfName);
		if (itf != null) {
			if (((PAGCMInterfaceType) itf.getFcItfType()).isGCMMulticastItf()) {
			// TODO: this is not ok, fin other way to deal with multicast interfaces.
			return getMulticastBindComponenents(component, itfName)[0];
			} else {
				return itf.getFcItfOwner();
			}
		}
		
		return null;
	}

	/**
	 * Returns the array of components bound the "multicastItfName" multicast interface, or null if it fails
	 * to getting them.
	 * 
	 * @param component	The client component
	 * @param multicastItfName The name of the multicast interface
	 * @return Array of bound components, or null if it fails
	 */
	public static Component[] getMulticastBindComponenents(Component component, String multicastItfName) {
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


	/**
	 * This methods contains the block of code to be executed by the ExecutorController.
	 * @param component A reference to component in which, the ExecutorController
	 * that will execute this action, belongs to.
	 * @param typeFactory A reference to the ExecutorController's PAGCMTypeFactory
	 * @param genericFactory A reference to the ExecutorController's PAGenericFactory
	 * @return Any desired object
	 */
	public abstract Object execute(Component component, PAGCMTypeFactory typeFactory, PAGenericFactory genericFactory);

}
