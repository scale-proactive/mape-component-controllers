#!/bin/bash
export JCP="/user/mibanez/mape-component-controllers/build/*"
JCP="$JCP:/user/mibanez/mape-component-controllers/lib/*"
JCP="$JCP:/user/mibanez/gcmscript/dist/lib/*"
JCP="$JCP:/user/mibanez/programming-multiactivities/dist/lib/*"

export VM="-Dgcm.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Djava.security.manager"
VM="$VM -Djava.security.policy=/user/mibanez/programming-multiactivities/dist/proactive.java.policy"
VM="$VM -Dlog4j.configuration=file:/user/mibanez/programming-multiactivities/dist/proactive-log4j"
VM="$VM -Duser.home=/user/mibanez"
VM="$VM -Dproactive.home=/user/mibanez/programming-multiactivities"
VM="$VM -Djline.terminal=jline.UnsupportedTerminal"

export TEST=cl.niclabs.autonomic.examples.balancer.Test

java -cp $JCP $VM $TEST "$@"
