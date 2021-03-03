#!/bin/sh

#DONE
#s1
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01","name":"s1-h3-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.3", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
#s2
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02","name":"s2-h3-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.3", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
#s3
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03","name":"s3-h3-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.3", "active":"true", "actions":"output=3"}' http://localhost:8080/wm/staticentrypusher/json
#s4
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:04","name":"s4-h3-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.3", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
#s5
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:05","name":"s5-h3-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.3", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json

