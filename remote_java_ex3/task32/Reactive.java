package net.sdnlab.ex3.task32;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.linkdiscovery.internal.LinkInfo;
import net.floodlightcontroller.routing.BroadcastTree;
import net.floodlightcontroller.topology.TopologyInstance;

public class Reactive implements IFloodlightModule, IOFMessageListener {
	
	private static final int MAX_LINK_WEIGHT = 10000;
	private static final int MAX_PATH_WEIGHT = Integer.MAX_VALUE - MAX_LINK_WEIGHT - 1;
	private static final int HOPCOUNT = 1;
	private static final int UTILIZATION = 2;
	private static final Logger logger = Logger.getLogger(Reactive.class.getSimpleName());

	// Since we are listening to OpenFlow messages we need to register with the FloodlightProvider (IFloodlightProviderService class)
	protected IFloodlightProviderService floodlightProvider;
	protected IOFSwitchService switchService;
	protected ILinkDiscoveryService linkDiscoverer;

	private Set<DatapathId> setDpids;
	private Map<DatapathId, Set<Link>> mapSwitchLinks;

	// static 
	@Override
	public String getName() {
		// DONE Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// DONE Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// DONE Auto-generated method stub
		return false;
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// DONE Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// DONE Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// DONE Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}
	
	private void setupLogger() {
		// DONE: export logger output into log file
		try {
			FileHandler fileHandler = new FileHandler("/home/student/ex3/task32.log");
			logger.addHandler(fileHandler);
		} catch (Exception e) {
	        System.out.println("Failed to configure logging to file");
	    }
	}
	
	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// DONE Auto-generated method stub
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		linkDiscoverer = context.getServiceImpl(ILinkDiscoveryService.class);
		setupLogger();
		logger.info("Init");
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// DONE Auto-generated method stub
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		logger.info("Start Up");
		enableReactiveRouting();
		logger.info("Start Up End");
	}

	private void enableReactiveRouting() {
		// STEP-1: link state detection: create directed graph for djikstra
		createNetworkGraph();
		// TODO: STEP-2: calculate shortest path: using dijkstra

		// TODO: STEP-3: create flow

		// TODO: STEP-4: install flow (when PACKET_IN ? or install right away

		// TODO: verification with ping and routetrace
	}

	// DONE: STEP-1: link state detection: create directed graph for djikstra
	private void createNetworkGraph() {
		logger.info("Get all DatapathID");
		setDpids = switchService.getAllSwitchDpids();

		// TODO: find out a way to avoid this part
		// wait until all the switches has been add to network
		while (setDpids.size() < 10) {
			setDpids = switchService.getAllSwitchDpids();
		}

		logger.info("Total switch number: " + setDpids.size());
		logger.info("Printing all DatapathID");

		for (DatapathId dpid: setDpids) {
			logger.info(dpid.toString());
		}

		logger.info("Getting switch-link map");

		mapSwitchLinks = linkDiscoverer.getSwitchLinks();
		while (mapSwitchLinks.size() < 10) {
			mapSwitchLinks = linkDiscoverer.getSwitchLinks();
			logger.info("Current switch links map size: " + mapSwitchLinks.size());
		}

		logger.info("Get all links");

		// TODO: remove this method to a more steady state
		for (DatapathId dpid : setDpids) {
			logger.info("Links of switch" + dpid.toString());
			mapSwitchLinks.containsKey(dpid);
			for (Link link : mapSwitchLinks.get(dpid)) {
				logger.info(link.toString());
			}
		}
	}

	// TODO: STEP-2: calculate shortest path: using dijkstra
	private void calShortestPath() {

	}

	// TODO: STEP-3: ceate flow
	private void createFlow() {

	}

	// TODO: STEP-4: install flow (when PACKET_IN ? or install right away
	private void installFlow() {

	}

	/*
	 * Creates a map of links and the cost associated with each link
	 *  Reference implementation: https://github.com/floodlight/floodlight/blob/d737cb05656a6038f4e2277ffb4503d45b7b29cb/src/main/java/net/floodlightcontroller/topology/TopologyInstance.java#L657
	 * */
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

	// reference: https://github.com/floodlight/floodlight/blob/d737cb05656a6038f4e2277ffb4503d45b7b29cb/src/main/java/net/floodlightcontroller/topology/TopologyInstance.java#L444
	private class NodeDist implements Comparable<NodeDist> {
		private final DatapathId node;
		public DatapathId getNode() {
			return node;
		}

		private final int dist;
		public int getDist() {
			return dist;
		}

		public NodeDist(DatapathId node, int dist) {
			this.node = node;
			this.dist = dist;
		}

		@Override
		public int compareTo(NodeDist o) {
			if (o.dist == this.dist) {
				return (int)(this.node.getLong() - o.node.getLong());
			}
			return this.dist - o.dist;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NodeDist other = (NodeDist) obj;
			if (node == null) {
				if (other.node != null)
					return false;
			} else if (!node.equals(other.node))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
			return 42;
		}
	}

	/*
	 * Dijkstra that calculates destination rooted trees over the entire topology.
	 * reference implementation: https://github.com/floodlight/floodlight/blob/d737cb05656a6038f4e2277ffb4503d45b7b29cb/src/main/java/net/floodlightcontroller/topology/TopologyInstance.java#L578
	 */
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

			if (cdist >= MAX_PATH_WEIGHT) break;
			if (seen.containsKey(cnode)) continue;
			seen.put(cnode, true);

			//log.debug("cnode {} and links {}", cnode, links.get(cnode));
			if (links.get(cnode) == null) continue;
			for (Link link : links.get(cnode)) {
				DatapathId neighbor;

				if (isDstRooted == true) {
					neighbor = link.getSrc();
				} else {
					neighbor = link.getDst();
				}

				// links directed toward cnode will result in this condition
				if (neighbor.equals(cnode)) continue;

				if (seen.containsKey(neighbor)) continue;

				if (linkCost == null || linkCost.get(link) == null) {
					w = 1;
				} else {
					w = linkCost.get(link);
				}

				int ndist = cdist + w; // the weight of the link, always 1 in current version of floodlight.
				logger.info("Neighbor: " + neighbor);
				logger.info("Cost: "+ cost);
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
