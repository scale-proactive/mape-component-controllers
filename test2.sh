#!/bin/bash
export JAVA="$JAVA_HOME/bin/java -cp /user/mibanez/home/Taller/programming-multiactivities/dist/lib/*"
JAVA="$JAVA:/user/mibanez/home/Taller/gcmscript/dist/lib/*"
JAVA="$JAVA:/user/mibanez/home/Taller/mape-component-controllers/build/*"
export VM="-Dgcm.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"
VM="$VM -Djava.security.manager"
VM="$VM -Djava.security.policy=/user/mibanez/home/Taller/programming-multiactivities/dist/proactive.java.policy"
VM="$VM -Dlog4j.configuration=file:/user/mibanez/home/Taller/programming-multiactivities/dist/proactive-log4j"
VM="$VM -Duser.home=/user/mibanez/home"
VM="$VM -Dproactive.home=/user/mibanez/home/Taller/programming-multiactivities/"

export TEST=examples.TestAAS

$JAVA $VM $TEST "$@"
