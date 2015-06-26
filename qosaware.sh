#!/bin/bash
SPRINGOO_HOME="/user/cruz/git/mape-component-controllers"

export JCP="$SPRINGOO_HOME/build/*"
JCP="$JCP:$SPRINGOO_HOME/lib/*"
JCP="$JCP:$SPRINGOO_HOME/lib/gcmscript-lib/*"
JCP="$JCP:$SPRINGOO_HOME/lib/pa-ma-lib/*"

export VM="-Dgcm.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Djava.security.manager"
VM="$VM -Djava.security.policy=$SPRINGOO_HOME/lib/pol-sec/proactive.java.policy"
VM="$VM -Dlog4j.configuration=file:$SPRINGOO_HOME/lib/pol-sec/proactive-log4j"
#VM="$VM -Duser.home=/user/cruz/git"
#VM="$VM -Dproactive.home=/user/cruz/git/programming-multiactivities"
VM="$VM -Djline.terminal=jline.UnsupportedTerminal"

export TEST=cl.niclabs.autonomic.examples.qosaware.Test

java -cp $JCP $VM $TEST "$@"

