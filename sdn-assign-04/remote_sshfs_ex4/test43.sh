#!/bin/sh
#curl http://10.10.10.10:8080/subscriptions/name/json

# Attributes {host, name, type, filter, ref_val, comparator}
# example
curl -X POST -d '{"udp_port":"50001", "type":"0", "filter_enable":"true", "reference_value":"10", "is_greater":"true"}' http://10.10.10.10:8080/subscriptions/sub1/json