QoS-Aware Springoo Application using GCM/MAPE Component Controllers
===================================================================

This code contains an example implementation of the Springoo component application
using MAPE Component Controllers to provide QoS Awareness.

## Requisites
ProActive library (included in $SPRINGOO\_HOME/lib/pa-ma-lib/)

GCMScript library (included in $SPRINGOO\_HOME/lib/gcmscript-lib/)

## Compiling
ant build.xml

This create the build/ directory with the compiled classes

## Running the Springoo Application
In the file `qosaware.sh`, update the variable $SPRINGOO\_HOME to the path to your mape-component-controllers directory.

Once the paths are set, the test is started by running `qosaware.sh`.

After the application is set up and running, the script `client.sh` can be used
to start tests on the Springoo Application.

`client.sh` receives a single argument that is the RMI address of the running application.
This address is reported by the Springoo application on startup.

```
./client.sh -c ActiveObject_org.objectweb.proactive.core.component.type.Composite_7f3ff946-14df58cc9fb--7ff3--f3f4bda1ec2be496-7f3ff946-14df58cc9fb--8000
```

Once started, the Springoo application will process requests from `client.sh`, and will reporte on the time taken by the request
on each component. The Springoo will monitor itself in order to adapt its parameters according to the response time measured.


## MAPE Component Controllers

MAPE (NF) Controllers are used to provide autonomic behavior to GCM/ProActive components.

The implementation uses a set of extension to GCMScript, detailed below

##### GCMScript extension commands:

- metrics:
  - add-metric(component_node, name, metric-class)
  - remove-metric(metric_node)
  - name(metric_node)
  - value(metric_node)
  - calculate(metric_node)
  - state(metric_node)
  - set-state(metric_node)

- rules:
  - add-rule(component_node, name, rule-class)
  - remove-rule(rule_node)
  - name(rule_node)
  - check(rule_node)

- subscriptions:
  - add-subscription(rule_node, metric_node)
  - remove-subscription(rule_node, metric_node)

- utils:
  - print-metrics(component_node)
  - print-rules(component_node)
