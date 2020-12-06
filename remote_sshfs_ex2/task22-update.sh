#!/bin/sh

#DONE

# create and install vlan for new configuration
#s3
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-src-sink-update", "priority":"2", "eth_vlan_vid":"0x1001", "active":"true", "actions":"pop_vlan,output=2"}' http://localhost:8080/wm/staticentrypusher/json
sleep 1
#s1
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-src-sink-update", "priority":"2", "eth_type":"0x0800", "eth_vlan_vid":"0x1001", "active":"true", "actions":"pop_vlan,set_field=eth_dst->00:00:00:00:00:02,set_field=ipv4_dst->192.168.1.2,output=1"}' http://localhost:8080/wm/staticentrypusher/json

sleep 1

# start updating on ingress switch
#s2
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-src-sink-update", "priority":"2", "eth_type":"0x0800", "ipv4_dst":"10.0.0.10", "active":"true", "actions":"push_vlan=0x8100,set_field=eth_vlan_vid->1,output=2,output=3"}' http://localhost:8080/wm/staticentrypusher/json
sleep 1


# deletes the old configuration when no matches for 10 seconds

# S2
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-src-sink-init", "priority":"1", "idle_timeout":"10", "eth_type":"0x0800", "ipv4_dst":"10.0.0.10", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
sleep 1

# S3
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-src-sink-init", "priority":"1", "idle_timeout":"10", "eth_type":"0x0800",  "ipv4_dst":"10.0.0.10", "active":"true", "actions":"output=2,output=3"}' http://localhost:8080/wm/staticentrypusher/json
sleep 1

# S4
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:04", "name":"s4-src-sink-init", "priority":"1", "idle_timeout":"10", "eth_type":"0x0800", "ipv4_dst":"10.0.0.10", "active":"true", "actions":"set_field=eth_dst->00:00:00:00:00:01,set_field=ipv4_dst->192.168.1.1,output=1"}' http://localhost:8080/wm/staticentrypusher/json
