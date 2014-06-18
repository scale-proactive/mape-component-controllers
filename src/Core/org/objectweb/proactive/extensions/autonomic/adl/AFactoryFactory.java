package org.objectweb.proactive.extensions.autonomic.adl;

import java.util.HashMap;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.proactive.core.component.adl.FactoryFactory;


public class AFactoryFactory {

    public final static String AUTONOMIC_FACTORY = "org.objectweb.proactive.extensions.autonomic.adl.AFactory";

    /**
     * Returns a factory for the GCM ADL. This factory will alsoP
     *
     * @see org.objectweb.fractal.adl.FactoryFactory#getFactory(java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Factory getAFactory() throws ADLException {
        return org.objectweb.fractal.adl.FactoryFactory.getFactory(AUTONOMIC_FACTORY,
        		FactoryFactory.PROACTIVE_BACKEND, new HashMap());
    }
    
}
