#!/bin/sh
# clear all previous command
curl http://localhost:8080/wm/staticentrypusher/clear/all/json

#DONE

#s2
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-src-sink-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.10", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
#s3
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-src-sink-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.10", "active":"true", "actions":"output=2,output=3"}' http://localhost:8080/wm/staticentrypusher/json
#s4, since 192.168.xxx.xxx and 10.0.xxx.xxx is for different 
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:04", "name":"s4-src-sink-init", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.10", "active":"true", "actions":"set_field=eth_dst->00:00:00:00:00:01,set_field=ipv4_dst->192.168.1.1,output=1"}' http://localhost:8080/wm/staticentrypusher/json


