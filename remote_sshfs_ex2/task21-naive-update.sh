#!/bin/sh

#DONE

# s1
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-h3-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.3", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
sleep 10

# s2
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-h3-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.3", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
sleep 10

# s5
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:05", "name":"s5-h3-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.3", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
sleep 10
