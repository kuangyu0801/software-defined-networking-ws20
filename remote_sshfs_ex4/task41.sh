#!/bin/sh
# Deletes all flow entries
curl http://localhost:8080/wm/staticentrypusher/clear/all/json

# S1 broadcasts ARP request
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-arpReq", "priority":"1", "eth_type":"0x0806", "arp_opcode":"0x1", "active":"true", "actions":"output=flood"}' http://localhost:8080/wm/staticentrypusher/json
# S1 forwards ARP reply
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-arpRep-pub1", "priority":"2", "eth_type":"0x0806","arp_opcode":"0x2", "arp_tpa":"10.1.0.1", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-arpRep-root", "priority":"2", "eth_type":"0x0806","arp_opcode":"0x2", "arp_tpa":"10.10.10.10", "active":"true", "actions":"output=3"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-arpRep-sub", "priority":"1", "eth_type":"0x0806","arp_opcode":"0x2", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
# S1 forwards IP pakcet
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-ip-root", "priority":"2", "eth_type":"0x0800", "ipv4_dst":"10.10.10.10", "active":"true", "actions":"output=3"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-ip-pub", "priority":"2", "eth_type":"0x0800", "ipv4_dst":"10.1.0.1", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-ip-sub", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.0/8", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json

# S2 broadcasts ARP request
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-arpReq", "priority":"1", "eth_type":"0x0806", "arp_opcode":"0x1", "active":"true", "actions":"output=flood"}' http://localhost:8080/wm/staticentrypusher/json
# S2 forwards ARP reply and ip packet
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-pub1", "priority":"1", "eth_dst":"00:00:00:00:00:01", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-sub1", "priority":"1", "eth_dst":"00:00:00:00:00:02", "active":"true", "actions":"output=3"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-sub2", "priority":"1", "eth_dst":"00:00:00:00:00:03", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-sub3", "priority":"1", "eth_dst":"00:00:00:00:00:04", "active":"true", "actions":"output=4"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-sub4", "priority":"1", "eth_dst":"00:00:00:00:00:05", "active":"true", "actions":"output=5"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-arpRep-root", "priority":"1", "eth_type":"0x0806","arp_opcode":"0x2", "arp_tpa":"10.10.10.10", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-ip-root", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.10.10.10", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json

# S3 forwards ARP request/ARP reply/IP packet, use IN_PORT as match
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-arpReq", "priority":"1", "eth_type":"0x0806", "arp_opcode":"0x1", "active":"true", "actions":"output=flood"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-arpRep-sub2", "priority":"2", "eth_type":"0x0806","arp_opcode":"0x2", "arp_tpa":"10.1.1.2", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-arpRep-other", "priority":"1", "eth_type":"0x0806","arp_opcode":"0x2", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-ip-sub2", "priority":"2", "eth_type":"0x0800", "ipv4_dst":"10.1.1.2", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-ip-other", "priority":"1", "eth_type":"0x0800", "ipv4_dst":"10.0.0.0/8", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json


# forward measurements to all subscribers
# S1
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-measurements", "priority":"2", "eth_type":"0x0800", "ipv4_dst":"230.0.0.0/8",  "ip_proto":"0x11", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
# S2
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:02", "name":"s2-measurements", "priority":"2", "eth_type":"0x0800", "ipv4_dst":"230.0.0.0/8", "ip_proto":"0x11", "active":"true", "actions":"output=2,set_field=ipv4_dst->10.1.1.1,set_field=udp_dst->50001,output=3,set_field=ipv4_dst->10.1.1.3,set_field=udp_dst->50003,output=4,set_field=ipv4_dst->10.1.1.4,set_field=udp_dst->50004,output=5,"}' http://localhost:8080/wm/staticentrypusher/json
# S3
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-measurements", "priority":"2", "eth_type":"0x0800", "ipv4_dst":"230.0.0.0/8", "ip_proto":"0x11", "active":"true", "actions":"set_field=ipv4_dst->10.1.1.2,set_field=udp_dst->50002,output=2"}' http://localhost:8080/wm/staticentrypusher/json

