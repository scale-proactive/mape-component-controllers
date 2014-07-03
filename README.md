mape-component-controllers
==========================

NF Controllers to provide autonomic behavior to GCM/ProActive components.

##### GCMScript extension commands:

- metrics:
  - add-metric(component_node, name, metric-class)
  - remove-metric(metric_node) [TODO]
  - name(metric_node)
  - value(metric_node)
  - calculate(metric_node)
  - state(metric_node)
  - set-state(metric_node)

- rules:
  - add-rule(component_node, name, rule-class)
  - remove-rule(rule_node) [TODO]
  - name(rule_node)
  - check(rule_node)

- subscriptions:
  - add-subscription(rule_node, metric_node)
  - remove-subscription(rule_node, metric_node)

- utils:
  - print-metrics(component_node)
  - print-rules(component_node)
