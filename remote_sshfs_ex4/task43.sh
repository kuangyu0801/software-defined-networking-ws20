#!/bin/sh
# Deletes all flow entries
curl http://10.10.10.10:8080/subscriptions/clear/all/json

# Attributes {host, name, type, filter, ref_val, comparator}
# example
# curl -X POST -d '{"host":"10.1.1.1", "name":"s1-sub", "type":"0", "filter":"true", "ref_val":"10", "comparator":"gt"}' http://localhost:8080/wm/staticentrypusher/json
