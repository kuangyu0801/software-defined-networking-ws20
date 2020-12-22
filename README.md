# 3.1 – Centralized ARP Handling

# 3.2 – Reactive Routing

## 實作

- Steps:
    - [TODO] link state probing : create graph
    - [TODO] calculate shortest path
    - [TODO] create flow when PACKET_IN
    - [TODO] install flow
    - [TODO] (test with trace route)

- layer 3 match
- the shortest path
    + hop as weight
    + dijkstra implementation maybe from _net.floodlightcontroller.topology.TopologyInstance.java_
    + dijkstra: https://github.com/floodlight/floodlight/blob/d737cb05656a6038f4e2277ffb4503d45b7b29cb/src/main/java/net/floodlightcontroller/topology/TopologyInstance.java#L578
    + cost map: https://github.com/floodlight/floodlight/blob/d737cb05656a6038f4e2277ffb4503d45b7b29cb/src/main/java/net/floodlightcontroller/topology/TopologyInstance.java#L657
    + 基本上來說是用priority queue, 如果link有update cost, 就要把舊的node刪掉, 插入新的node (但其實好像可以用一個叫做indexed queue的東西...) 
- graph may be hard-coded or dynamically build (first hardcode to test dijkstra then replace with dynamic graph)
    + Hint for the usage of ILinkDiscoveryService
        + Wait until topology is detected
        + return Command.CONTINUE
        
## 會用到的物件
- DatapathId (Map Key, Switch ID): http://floodlight.github.io/floodlight/javadoc/openflowj-loxi/org/projectfloodlight/openflow/types/DatapathId.html
    + http://floodlight.github.io/floodlight/javadoc/openflowj-loxi/org/projectfloodlight/openflow/types/class-use/DatapathId.html
- Link: http://floodlight.github.io/floodlight/javadoc/floodlight/net/floodlightcontroller/linkdiscovery/Link.html
- BroadcastTree (Dijkstra return type): https://github.com/floodlight/floodlight/blob/d737cb05656a6038f4e2277ffb4503d45b7b29cb/src/main/java/net/floodlightcontroller/routing/BroadcastTree.java#L25
- Interface IOFSwitchService: http://floodlight.github.io/floodlight/javadoc/floodlight/net/floodlightcontroller/core/internal/IOFSwitchService.html
```
sudo mn --switch ovsk --controller remote,port=6653 --custom ~/ex3/fattree.py --topo fattree --arp
```

- 
# 3.3 – Adaptive Link Load Balancing

# Frequently Used Command

- Floodlight Services: https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/40402986/Floodlight+Services

```
ssh sdnfp04_proxy

sshfs sdnfp04_proxy:/home/student/ex3 remote_sshfs_ex3

sshfs sdnfp04_proxy:/opt/floodlight/src/main/java/net/sdnlab/ex3 remote_java_ex3
```