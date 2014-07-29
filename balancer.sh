#!/bin/bash
export JCP="/home/mibanez/Taller/memoria/mape-component-controllers/build/*"
JCP="$JCP:/home/mibanez/Taller/memoria/mape-component-controllers/lib/*"
JCP="$JCP:/home/mibanez/Taller/memoria/gcmscript/dist/lib/*"
JCP="$JCP:/home/mibanez/Taller/memoria/programming-multiactivities/dist/lib/*"

export VM="-Dgcm.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Djava.security.manager"
VM="$VM -Djava.security.policy=/home/mibanez/Taller/memoria/programming-multiactivities/dist/proactive.java.policy"
VM="$VM -Dlog4j.configuration=file:/home/mibanez/Taller/memoria/programming-multiactivities/dist/proactive-log4j"
VM="$VM -Duser.home=/home/mibanez"
VM="$VM -Dproactive.home=/home/mibanez/Taller/memoria/programming-multiactivities"
VM="$VM -Djline.terminal=jline.UnsupportedTerminal"

export TEST=cl.niclabs.autonomic.examples.balancer.Test

java -cp $JCP $VM $TEST "$@"
