import java.util.*;
import java.util.logging.Logger;

public class ReactiveDemo {
    private static final int MAX_LINK_WEIGHT = 10000;
    private static final int MAX_PATH_WEIGHT = Integer.MAX_VALUE - MAX_LINK_WEIGHT - 1;
    private static final int HOPCOUNT = 1;
    private static final int UTILIZATION = 2;
    private static final Logger logger = Logger.getLogger(ReactiveDemo.class.getSimpleName());

    public static Map<Link,Integer> initLinkCostMap(int type, Map<DatapathId, Set<Link>> links) {
        Map<Link, Integer> linkCost = new HashMap<Link, Integer>();
        //int tunnel_weight = portsWithLinks.size() + 1;

        switch (type){
/*			case HOPCOUNT_AVOID_TUNNELS:
				log.debug("Using hop count with tunnel bias for metrics");
				for (NodePortTuple npt : portsTunnel) {
					if (links.get(npt) == null) {
						continue;
					}
					for (Link link : links.get(npt)) {
						if (link == null) {
							continue;
						}
						linkCost.put(link, tunnel_weight);
					}
				}
				return linkCost;*/

            case HOPCOUNT:
                logger.info("Using hop count w/o tunnel bias for metrics");
                for (DatapathId npt : links.keySet()) {
                    if (links.get(npt) == null) {
                        continue;
                    }
                    for (Link link : links.get(npt)) {
                        if (link == null) {
                            continue;
                        }
                        linkCost.put(link,1);
                    }
                }
                return linkCost;

/*			case LATENCY:
				log.debug("Using latency for path metrics");
				for (NodePortTuple npt : links.keySet()) {
					if (links.get(npt) == null) {
						continue;
					}
					for (Link link : links.get(npt)) {
						if (link == null) {
							continue;
						}
						if ((int)link.getLatency().getValue() < 0 ||
								(int)link.getLatency().getValue() > MAX_LINK_WEIGHT) {
							linkCost.put(link, MAX_LINK_WEIGHT);
						} else {
							linkCost.put(link,(int)link.getLatency().getValue());
						}
					}
				}
				return linkCost;*/

/*			case LINK_SPEED:
				TopologyManager.statisticsService.collectStatistics(true);
				log.debug("Using link speed for path metrics");
				for (NodePortTuple npt : links.keySet()) {
					if (links.get(npt) == null) {
						continue;
					}
					long rawLinkSpeed = 0;
					IOFSwitch s = TopologyManager.switchService.getSwitch(npt.getNodeId());
					if (s != null) {
						OFPortDesc p = s.getPort(npt.getPortId());
						if (p != null) {
							rawLinkSpeed = p.getCurrSpeed();
						}
					}
					for (Link link : links.get(npt)) {
						if (link == null) {
							continue;
						}

						if ((rawLinkSpeed / 10^6) / 8 > 1) {
							int linkSpeedMBps = (int)(rawLinkSpeed / 10^6) / 8;
							linkCost.put(link, (1/linkSpeedMBps)*1000);
						} else {
							linkCost.put(link, MAX_LINK_WEIGHT);
						}
					}
				}
				return linkCost;*/

/*			case UTILIZATION:
				TopologyManager.statisticsService.collectStatistics(true);
				log.debug("Using utilization for path metrics");
				for (NodePortTuple npt : links.keySet()) {
					if (links.get(npt) == null) continue;
					SwitchPortBandwidth spb = TopologyManager.statisticsService
							.getBandwidthConsumption(npt.getNodeId(), npt.getPortId());
					long bpsTx = 0;
					if (spb != null) {
						bpsTx = spb.getBitsPerSecondTx().getValue();
					}
					for (Link link : links.get(npt)) {
						if (link == null) {
							continue;
						}

						if ((bpsTx / 10^6) / 8 > 1) {
							int cost = (int) (bpsTx / 10^6) / 8;
							linkCost.put(link, cost);
						} else {
							linkCost.put(link, MAX_LINK_WEIGHT);
						}
					}
				}
				return linkCost;*/

            default:
                logger.info("Invalid Selection: Using Default Hop Count with Tunnel Bias for Metrics");
/*				for (NodePortTuple npt : portsTunnel) {
					if (links.get(npt) == null) continue;
					for (Link link : links.get(npt)) {
						if (link == null) continue;
						linkCost.put(link, tunnel_weight);
					}
				}*/
                return linkCost;
        }
    }

    private static BroadcastTree dijkstra(Map<DatapathId, Set<Link>> links, DatapathId root,
                                   Map<Link, Integer> linkCost,
                                   boolean isDstRooted) {
        HashMap<DatapathId, Link> nexthoplinks = new HashMap<DatapathId, Link>();
        HashMap<DatapathId, Integer> cost = new HashMap<DatapathId, Integer>();
        int w;

        for (DatapathId node : links.keySet()) {
            nexthoplinks.put(node, null);
            cost.put(node, MAX_PATH_WEIGHT);
            //log.debug("Added max cost to {}", node);
        }

        HashMap<DatapathId, Boolean> seen = new HashMap<DatapathId, Boolean>();
        PriorityQueue<NodeDist> nodeq = new PriorityQueue<NodeDist>();
        nodeq.add(new NodeDist(root, 0));
        cost.put(root, 0);

        //log.debug("{}", links);

        while (nodeq.peek() != null) {
            NodeDist n = nodeq.poll();
            DatapathId cnode = n.getNode();
            int cdist = n.getDist();

            if (cdist >= MAX_PATH_WEIGHT) {
                break;
            }

            if (seen.containsKey(cnode)) {
                continue;
            }

            seen.put(cnode, true);

            //log.debug("cnode {} and links {}", cnode, links.get(cnode));
            if (links.get(cnode) == null) {
                continue;
            }

            for (Link link : links.get(cnode)) {
                DatapathId neighbor = (link.getSrc().equals(cnode)) ? link.getDst() : link.getSrc();

                if (seen.containsKey(neighbor)) {
                    continue;
                }

                if (linkCost == null || linkCost.get(link) == null) {
                    w = 1;
                } else {
                    w = linkCost.get(link);
                }

                int ndist = cdist + w; // the weight of the link, always 1 in current version of floodlight.
                logger.info("Neighbor: " + neighbor.getLong());
                logger.info("Neighbor cost: " + cost.get(neighbor));

                if (ndist < cost.get(neighbor)) {
                    cost.put(neighbor, ndist);
                    nexthoplinks.put(neighbor, link);

                    NodeDist ndTemp = new NodeDist(neighbor, ndist);
                    // Remove an object that's already in there.
                    // Note that the comparison is based on only the node id,
                    // and not node id and distance.
                    nodeq.remove(ndTemp);
                    // add the current object to the queue.
                    nodeq.add(ndTemp);
                }
            }
        }

        BroadcastTree ret = new BroadcastTree(nexthoplinks, cost);

        return ret;
    }

    public static void main(String[] args) {
        DatapathId dpid1 = new DatapathId(1);
        DatapathId dpid2 = new DatapathId(2);
        DatapathId dpid101 = new DatapathId(101);
        DatapathId dpid102 = new DatapathId(102);
        DatapathId dpid103 = new DatapathId(103);
        DatapathId dpid104 = new DatapathId(104);
        DatapathId dpid201 = new DatapathId(201);
        DatapathId dpid202 = new DatapathId(202);
        DatapathId dpid203 = new DatapathId(203);
        DatapathId dpid204 = new DatapathId(204);

        Map<DatapathId, Set<Link>> mapLinks = new HashMap<>();

        Link[] links = new Link[16];

        // 1 -> 1.3, 1.4, 2.3, 2,4
        links[0] = new Link(dpid1, dpid103);
        links[1] = new Link(dpid1, dpid104);
        links[2] = new Link(dpid1, dpid203);
        links[3] = new Link(dpid1, dpid204);

        // 2 -> 1.3, 1.4, 2.3, 2,4
        links[4] = new Link(dpid2, dpid103);
        links[5] = new Link(dpid2, dpid104);
        links[6] = new Link(dpid2, dpid203);
        links[7] = new Link(dpid2, dpid204);

        // 1.1 -> 1.3, 1.4
        links[8] = new Link(dpid101, dpid103);
        links[9] = new Link(dpid101, dpid104);
        // 1.2 -> 1.3, 1.4
        links[10] = new Link(dpid102, dpid103);
        links[11] = new Link(dpid102, dpid104);

        // 2.1 -> 2.3, 2.4
        links[12] = new Link(dpid201, dpid203);
        links[13] = new Link(dpid201, dpid204);
        links[14] = new Link(dpid202, dpid203);
        links[15] = new Link(dpid202, dpid204);

        for (Link link : links) {
            DatapathId src = link.src;
            DatapathId dst = link.dst;
            mapLinks.putIfAbsent(src, new HashSet<>());
            mapLinks.putIfAbsent(dst, new HashSet<>());
            mapLinks.get(src).add(link);
            mapLinks.get(dst).add(link);
        }

        for (DatapathId dpid : mapLinks.keySet()) {
            System.out.println(dpid.getLong() + " has following neighbor: ");
            for (Link link : mapLinks.get(dpid)) {
                if (link.src == dpid) {
                    System.out.println(link.dst.getLong());
                } else {
                    System.out.println(link.src.getLong());
                }
            }
        }

        Map<Link, Integer> linkCost = initLinkCostMap(HOPCOUNT, mapLinks);
        // DONE: there are no links in broadcastTree
        BroadcastTree broadcastTree = dijkstra(mapLinks, dpid101, linkCost, false);
        for (DatapathId dpid : broadcastTree.links.keySet()) {
            Link link = broadcastTree.links.get(dpid);
            if (link != null) {
                System.out.print("Node: " + dpid.getLong());
                System.out.println(", Cost: " + broadcastTree.costs.get(dpid));
                System.out.println("Link: " + link.src.getLong() + "---" + link.dst.getLong());
            }
        }
    }
}
