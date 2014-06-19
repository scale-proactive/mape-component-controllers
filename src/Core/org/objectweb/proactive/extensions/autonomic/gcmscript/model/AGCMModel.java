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

import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.fscript.model.Model;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.fractal.fscript.procedures.NativeLibrary;
import org.objectweb.proactive.extra.component.fscript.model.GCMModel;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;
import org.objectweb.proactive.extra.component.fscript.model.GCMProcedure;


/**
 * This class represents the GCM component model in terms of the {@link Model} APIs. It describes which
 * kinds of nodes are present in a GCM architecture, what properties these nodes have, and what axes can
 * connect these nodes together to form a complete GCM architecture. It also provides all the
 * introspection and reconfiguration procedures implied by this description (through its {@link NativeLibrary}
 * interface).
 * <br>
 * This is an extension of the {@link FractalModel} to take care of GCM specificities.
 *
 * @author The ProActive Team
 */
public class AGCMModel extends GCMModel implements GCMNodeFactory, BindingController {

    /**
     * Contributes a few custom procedures to manipulate GCM architecture which can not be described and
     * generated in the framework of the {@link Model} APIs.
     */
    @Override
    protected void createAdditionalProcedures() {
        super.createAdditionalProcedures();

        List<GCMProcedure> procedures = new ArrayList<GCMProcedure>();
        procedures.add(new AGCMNewAction());
        for (GCMProcedure procedure : procedures) {
            try {
                procedure.bindFc(GCMProcedure.MODEL_NAME, this);
            } catch (Exception e) {
                throw new AssertionError("Internal inconsistency with " + procedure.getName() + " procedure");
            }
            addProcedure(procedure);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "A GCM model";
    }
}
