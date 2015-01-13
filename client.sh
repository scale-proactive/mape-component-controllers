#!/bin/bash
export JCP="/user/cruz/git/mape-component-controllers/build/*"
JCP="$JCP:/user/cruz/git/mape-component-controllers/lib/*"
JCP="$JCP:/user/cruz/git/gcmscript/dist/lib/*"
JCP="$JCP:/user/cruz/git/programming-multiactivities/dist/lib/*"

export VM="-Dgcm.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Djava.security.manager"
VM="$VM -Djava.security.policy=/user/cruz/git/programming-multiactivities/dist/proactive.java.policy"
VM="$VM -Dlog4j.configuration=file:/user/cruz/git/programming-multiactivities/dist/proactive-log4j"
VM="$VM -Duser.home=/user/cruz"
VM="$VM -Dproactive.home=/user/cruz/git/programming-multiactivities"
VM="$VM -Djline.terminal=jline.UnsupportedTerminal"
VM="$VM -Dproactive.runtime.ping=false"

export TEST=cl.niclabs.autonomic.examples.balancer.Client

java -cp $JCP $VM $TEST "$@"
