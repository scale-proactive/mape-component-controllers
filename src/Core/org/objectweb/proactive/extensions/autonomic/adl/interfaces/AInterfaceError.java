package org.objectweb.proactive.extensions.autonomic.adl.interfaces;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;

public enum AInterfaceError implements ErrorTemplate {

	MISSING_CONTROLLER_DESCRIPTION("The controller description (composite or primitive) MUST be declared.");
	
	public static final String GROUP_ID = InterfaceErrors.GROUP_ID;
	
    private int id;
    private String format;
   
	private AInterfaceError(final String format, final Object... args) {
        this.id = ordinal();
        this.format = format;

        assert validErrorTemplate(this, args);
    }
	
    public int getErrorId() {
        return id;
    }

    public String getGroupId() {
        return GROUP_ID;
    }

    public String getFormatedMessage(final Object... args) {
        return String.format(format, args);
    }

    public String getFormat() {
        return format;
    }

}
