/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.ast.SourceLocation;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.procedures.NativeProcedure;
import org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.autonomic.adl.AFactory;
import org.objectweb.proactive.extra.component.fscript.model.GCMApplicationNode;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;
import org.objectweb.proactive.extra.component.fscript.model.GCMNewAction;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeNode;
import org.objectweb.proactive.extra.component.fscript.model.GCMVirtualNodeNode;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * A GCM procedure to implement the <code>gcm-new-autonomic()</code> action which instantiates a GCM component, with
 * the autonomic component controllers inside its membrane.
 *
 * @author The ProActive Team
 */
public class AGCMNewAction extends GCMNewAction implements NativeProcedure, BindingController {
    /**
     * Returns the name of the procedure.
     *
     * @return The name of the procedure.
     */
	@Override
    public String getName() {
        return "gcm-new-autonomic";
    }

    /**
     * Creates a {@link GCMComponentNode} representing a new GCM component.
     *
     * @param args The arguments of the procedure call. Must contain as first element the ADL name of the GCM
     * component to instantiate and as second element a {@link GCMApplicationNode} representing a
     * {@link GCMApplication} or a set of {@link GCMVirtualNodeNode} representing a set of {@link GCMVirtualNode}
     * or a set of {@link GCMNodeNode} representing a set of GCM {@link Node} to be given to the context for
     * instantiate the new GCM component.
     * @param ctx The execution context in which to execute the procedure.
     * @return The {@link GCMComponentNode} representing the new GCM component.
     * @throws ScriptExecutionError If any error occurred during the execution of the procedure.
     */
    public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {
        String adlName = (String) args.get(0);
        try {
            String[] addedKeys = new String[0];
            if (args.get(1) instanceof GCMApplicationNode) {
                GCMApplication gcma = ((GCMApplicationNode) args.get(1)).getGCMApplication();
                addedKeys = new String[] { "deployment-descriptor" };
                model.getInstanciationContext().put(addedKeys[0], gcma);
            } else {
                Object[] nodeContainers = ((Set<?>) args.get(1)).toArray();
                if ((nodeContainers.length > 0) && (nodeContainers[0] instanceof GCMVirtualNodeNode)) {
                    addedKeys = new String[nodeContainers.length];
                    for (int i = 0; i < nodeContainers.length; i++) {
                        GCMVirtualNode gcmvn = ((GCMVirtualNodeNode) nodeContainers[i]).getGCMVirtualNode();
                        addedKeys[i] = gcmvn.getName();
                        model.getInstanciationContext().put(addedKeys[i], gcmvn);
                    }
                } else {
                    addedKeys = new String[] { ADLNodeProvider.NODES_ID };
                    List<Node> gcmnodes = new ArrayList<Node>(nodeContainers.length);
                    for (int i = 0; i < nodeContainers.length; i++) {
                        gcmnodes.add(((GCMNodeNode) nodeContainers[i]).getGCMNode());
                    }
                    model.getInstanciationContext().put(addedKeys[0], gcmnodes);
                }
            }
            Component newComp = (Component) getFactory().newAutonomicComponent(adlName,
                    model.getInstanciationContext());
            for (int i = 0; i < addedKeys.length; i++) {
                model.getInstanciationContext().remove(addedKeys[i]);
            }
            GCMNodeFactory nf = (GCMNodeFactory) model;
            return nf.createGCMComponentNode(newComp);
        } catch (Exception ae) {
            throw new ScriptExecutionError(Diagnostic.error(SourceLocation.UNKNOWN,
                    "Unable to instantiate GCM component: " + adlName), ae);
        }
    }

    /**
     * Returns the {@link Factory} to use to instantiate GCM component.
     *
     * @return The {@link Factory} to use to instantiate GCM component.
     */
    private AFactory getFactory() {
        try {
            return (AFactory) model.lookupFc("gcm-factory");
        } catch (NoSuchInterfaceException nsie) {
            throw new AssertionError("Invalid GCM Model component");
        }
    }
}
