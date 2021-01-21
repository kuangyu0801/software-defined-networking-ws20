#!/bin/sh
# Deletes all flow entries
#curl http://10.10.10.10:8080/subscriptions/clear/all/json
curl http://localhost:8080/wm/staticentrypusher/clear/all/json

# Attributes {host, name, type, filter, ref_val, comparator}
# example
# curl -X POST -d '{"host":"10.1.1.1", "name":"s1-sub", "type":"0", "filter":"true", "ref_val":"10", "comparator":"gt"}' http://localhost:8080/wm/staticentrypusher/json

# S1 broadcasts ARP request
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-arpReq", "priority":"1", "eth_type":"0x0806", "arp_opcode":"0x1", "active":"true", "actions":"output=flood"}' http://localhost:8080/wm/staticentrypusher/json
# S1 forwards ARP reply
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-arpRep-pub1", "priority":"2", "eth_type":"0x0806","arp_opcode":"0x2", "arp_tpa":"10.1.0.1", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-arpRep-root", "priority":"2", "eth_type":"0x0806","arp_opcode":"0x2", "arp_tpa":"10.10.10.10", "active":"true", "actions":"output=3"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-arpRep-sub", "priority":"1", "eth_type":"0x0806","arp_opcode":"0x2", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
# S1 forwards IP pakcet
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-ip-root", "priority":"2", "eth_type":"0x0800", "ipv4_dst":"10.10.10.10", "active":"true", "actions":"output=3"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-ip-pub", "priority":"2", "eth_type":"0x0800", "ipv4_dst":"10.1.0.1", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:01", "name":"s1-ip-sub", "priority":"1", "eth_type":"0x0800", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json

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
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-sub2", "priority":"1", "in_port":"1", "active":"true", "actions":"output=2"}' http://localhost:8080/wm/staticentrypusher/json
curl -X POST -d '{"switch":"00:00:00:00:00:00:00:03", "name":"s3-other", "priority":"1", "in_port":"2", "active":"true", "actions":"output=1"}' http://localhost:8080/wm/staticentrypusher/json
