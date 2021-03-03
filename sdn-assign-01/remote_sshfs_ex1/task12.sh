#!/bin/sh
#Delete all static flow entries
curl http://localhost:8080/wm/staticentrypusher/clear/all/json

#vlan1 s1
curl -X POST -d '{"name":"s1_vlan1_in", "switch":"00:00:00:00:00:00:00:01", "in_port":"2",  "instruction_apply_actions":"push_vlan=0x8100,set_field=eth_vlan_vid->1,output=1"}' http://localhost:8080/wm/staticentrypusher/json

curl -X POST -d '{"name":"s1_vlan1_out", "switch":"00:00:00:00:00:00:00:01", "in_port":"1", "eth_vlan_vid":"0x1001", "actions":"pop_vlan,output=2"}' http://localhost:8080/wm/staticentrypusher/json

#vlan2 s1
curl -X POST -d '{"name":"s1_vlan2_in", "switch":"00:00:00:00:00:00:00:01", "in_port":"3",  "instruction_apply_actions":"push_vlan=0x8100,set_field=eth_vlan_vid->2,output=1"}' http://localhost:8080/wm/staticentrypusher/json

curl -X POST -d '{"name":"s1_vlan2_out", "switch":"00:00:00:00:00:00:00:01", "in_port":"1", "eth_vlan_vid":"0x1002", "actions":"pop_vlan,output=3"}' http://localhost:8080/wm/staticentrypusher/json

#vlan1 s2
curl -X POST -d '{"name":"s2_vlan1_in", "switch":"00:00:00:00:00:00:00:02", "in_port":"3", "instruction_apply_actions":"push_vlan=0x8100,set_field=eth_vlan_vid->1,output=1"}' http://localhost:8080/wm/staticentrypusher/json

curl -X POST -d '{"name":"s2_vlan1_out", "switch":"00:00:00:00:00:00:00:02", "in_port":"1", "eth_vlan_vid":"0x1001", "actions":"pop_vlan,output=3"}' http://localhost:8080/wm/staticentrypusher/json

#vlan2 s2
curl -X POST -d '{"name":"s2_vlan2_in", "switch":"00:00:00:00:00:00:00:02", "in_port":"2", "instruction_apply_actions":"push_vlan=0x8100,set_field=eth_vlan_vid->2,output=1"}' http://localhost:8080/wm/staticentrypusher/json

curl -X POST -d '{"name":"s2_vlan2_out", "switch":"00:00:00:00:00:00:00:02", "in_port":"1", "eth_vlan_vid":"0x1002", "actions":"pop_vlan,output=2"}' http://localhost:8080/wm/staticentrypusher/json

