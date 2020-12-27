# 3.1 – Centralized ARP Handling

-
```
sudo mn --switch ovsk --controller remote,port=6653 --custom ~/ex3/task14topo.py --topo task14topo
```
# 3.2 – Reactive Routing

## 實作

- Steps:
    - [DONE] link state probing : create graph
    - [TODO] calculate shortest path
    - [TODO] create flow when PACKET_IN
    - [TODO] install flow
    - [TODO] (test with trace route)
    - [TODO] write python parsing script for xml

- layer 3 match
- the shortest path
    + hop as weight
    + dijkstra implementation maybe from _net.floodlightcontroller.topology.TopologyInstance.java_
    + dijkstra: https://github.com/floodlight/floodlight/blob/d737cb05656a6038f4e2277ffb4503d45b7b29cb/src/main/java/net/floodlightcontroller/topology/TopologyInstance.java#L578
        + dijkstra demo slides from CS61B: https://docs.google.com/presentation/d/1_bw2z1ggUkquPdhl7gwdVBoTaoJmaZdpkV6MoAgxlJc/pub?start=false&loop=false&delayms=3000&slide=id.g771336078_0_180
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
    + getSwitch()
    + getAllSwitchDpids()
- Interface ILinkDiscoveryService http://floodlight.github.io/floodlight/javadoc/floodlight/net/floodlightcontroller/linkdiscovery/ILinkDiscoveryService.html
    + getSwitchLinks()
- IOFSwitch http://floodlight.github.io/floodlight/javadoc/floodlight/net/floodlightcontroller/core/IOFSwitch.html    
```
sudo mn --switch ovsk --controller remote,port=6653 --custom ~/ex3/fattree.py --topo fattree --arp
```

- 
# 3.3 – Adaptive Link Load Balancing

# Frequently Used Command
- 改用java native library 
    + https://codebeautify.org/
- Java程序员修炼之道 之 Logging(2/3) - 怎么写Log https://blog.csdn.net/justfly/article/details/38525335
- Java日志记录最佳实践 https://www.jianshu.com/p/546e9aace657
- http://www.slf4j.org/manual.html
- Floodlight Services: https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/40402986/Floodlight+Services

```
ssh sdnfp04_proxy

sshfs sdnfp04_proxy:/home/student/ex3 remote_sshfs_ex3

sshfs sdnfp04_proxy:/opt/floodlight/src/main/java/net/sdnlab/ex3 remote_java_ex3
```

# Logger
```
// using traditional API
logger.debug("Temperature set to {}. Old temperature was {}.", newT, oldT);
```
# Curiosity
- private, protected, public privilege difference
- package and project relation
- enumeration with java

