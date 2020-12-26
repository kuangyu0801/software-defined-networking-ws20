import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Logger;

public class ReactiveDemo {
    private static final int MAX_LINK_WEIGHT = 10000;
    private static final int MAX_PATH_WEIGHT = Integer.MAX_VALUE - MAX_LINK_WEIGHT - 1;
    private static final int HOPCOUNT = 1;
    private static final int UTILIZATION = 2;
    private static final Logger logger = Logger.getLogger(ReactiveDemo.class.getSimpleName());

    public Map<Link,Integer> initLinkCostMap(int type, Map<DatapathId, Set<Link>> links) {
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

    private BroadcastTree dijkstra(Map<DatapathId, Set<Link>> links, DatapathId root,
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
                DatapathId neighbor;

                if (isDstRooted == true) {
                    neighbor = link.getSrc();
                } else {
                    neighbor = link.getDst();
                }

                // links directed toward cnode will result in this condition
                if (neighbor.equals(cnode)) {
                    continue;
                }

                if (seen.containsKey(neighbor)) {
                    continue;
                }

                if (linkCost == null || linkCost.get(link) == null) {
                    w = 1;
                } else {
                    w = linkCost.get(link);
                }

                int ndist = cdist + w; // the weight of the link, always 1 in current version of floodlight.
                logger.info("Neighbor: " + neighbor);
                logger.info("Cost: " + cost);
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
}
