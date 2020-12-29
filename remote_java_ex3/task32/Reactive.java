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
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;

import javafx.util.Pair;
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
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
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

	private Set<DatapathId> dpidSet;
	private Map<DatapathId, Set<Link>> switchLinkMap;
	// to look up host with its connecting switch and port
	private Map<IPv4Address, Pair<DatapathId, OFPort>> edgeSwitchMap;
	// to look up shortest path wtih root Minimum spanning tree of that root
	private Map<IPv4Address, BroadcastTree> rootMstMap;
	private Map<Link,Integer> linkCostMap;
	
	protected Reactive() {
		
	}
	
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

	private static DatapathId getLinkOtherEnd(DatapathId src, Link link) {
		return (link.getSrc().equals(src)) ? link.getDst() : link.getSrc();
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		// Inject the first packet directly at the target switch
		switch(msg.getType()) {
			case PACKET_IN:
				Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
				IPv4 ipv4Pkt= (IPv4) eth.getPayload();
				IPv4Address srcAddr = ipv4Pkt.getSourceAddress();
				if (!rootMstMap.containsKey(srcAddr)) {
					calShortestPath(srcAddr);
					createFlow(srcAddr);
					installFlow(srcAddr);
				}
				break;
			default:
				break;
		}
		return Command.CONTINUE;
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
		l.add(ILinkDiscoveryService.class);
		l.add(IOFSwitchService.class);
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
		setRoutingTables();
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
		dpidSet = switchService.getAllSwitchDpids();

		// TODO: find out a way to avoid this part
		// wait until all the switches has been add to network
		while (dpidSet.size() < 10) {
			dpidSet = switchService.getAllSwitchDpids();
		}

		logger.info("Total switch number: " + dpidSet.size());
		logger.info("Printing all DatapathID");

		for (DatapathId dpid: dpidSet) {
			logger.info(dpid.toString());
		}

		logger.info("Getting switch-link map");

		switchLinkMap = linkDiscoverer.getSwitchLinks();
		while (switchLinkMap.size() < 10) {
			switchLinkMap = linkDiscoverer.getSwitchLinks();
			logger.info("Current switch links map size: " + switchLinkMap.size());
		}

		logger.info("Get all links");

		// TODO: remove this method to a more steady state
		for (DatapathId dpid : dpidSet) {
			logger.info("Links of switch" + dpid.toString());
			switchLinkMap.containsKey(dpid);
			for (Link link : switchLinkMap.get(dpid)) {
				logger.info(link.toString());
			}
		}
	}

	// TODO: STEP-2: calculate shortest path: using dijkstra
	private void calShortestPath(IPv4Address srcAddr) {

	}

	// TODO: STEP-3: ceate flow
	private void createFlow(IPv4Address srcAddr) {

	}

	// TODO: STEP-4: install flow when PACKET_IN event (reactivly)
	private void installFlow(IPv4Address srcAddr) {

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
	private static class NodeDist implements Comparable<NodeDist> {
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
	private static BroadcastTree dijkstra(DatapathId root, Map<DatapathId, Set<Link>> links, Map<Link, Integer> linkCost) {
		HashMap<DatapathId, Link> nexthoplinks = new HashMap<DatapathId, Link>();
		HashMap<DatapathId, Integer> cost = new HashMap<DatapathId, Integer>();
		int w;

		for (DatapathId node : links.keySet()) {
			nexthoplinks.put(node, null);
			cost.put(node, MAX_PATH_WEIGHT);
			//log.debug("Added max cost to {}", node);
		}

		HashMap<DatapathId, Boolean> seen = new HashMap<DatapathId, Boolean>();
		PriorityQueue<NodeDist> pqNode = new PriorityQueue<NodeDist>();
		NodeDist rootNode = new NodeDist(root, 0);
		pqNode.offer(rootNode);
		cost.put(root, 0);

		//log.debug("{}", links);

		while (pqNode.peek() != null) {
			NodeDist n = pqNode.poll();
			DatapathId curDpid = n.getNode();
			int curDist = n.getDist();

			if (curDist >= MAX_PATH_WEIGHT) {
				break;
			}

			if (seen.containsKey(curDpid)) {
				continue;
			}

			seen.put(curDpid, true);

			//log.debug("curDpid {} and links {}", curDpid, links.get(curDpid));
			if (links.get(curDpid) == null) {
				continue;
			}

			for (Link link : links.get(curDpid)) {
				DatapathId neighbor = getLinkOtherEnd(curDpid, link);

				if (seen.containsKey(neighbor)) {
					continue;
				}

				if (linkCost == null || linkCost.get(link) == null) {
					w = 1;
				} else {
					w = linkCost.get(link);
				}

				int ndist = curDist + w; // the weight of the link, always 1 in current version of floodlight.
				logger.info("Neighbor: " + neighbor.getLong());
				logger.info("Neighbor cost: " + cost.get(neighbor));

				if (ndist < cost.get(neighbor)) {
					cost.put(neighbor, ndist);
					nexthoplinks.put(neighbor, link);

					NodeDist ndTemp = new NodeDist(neighbor, ndist);
					// Remove an object that's already in there.
					// Note that the comparison is based on only the node id,
					// and not node id and distance.
					pqNode.remove(ndTemp);
					// add the current object to the queue.
					pqNode.add(ndTemp);
				}
			}
		}

		BroadcastTree ret = new BroadcastTree(nexthoplinks, cost);

		return ret;
	}

	public void setRoutingTables() {
		//Set up the routing table for first PACKET_IN inject
		edgeSwitchMap = new HashMap<>();
		edgeSwitchMap.put(IPv4Address.of("10.10.1.1"), new Pair<DatapathId, OFPort>(DatapathId.of(0x101), OFPort.of(3)));
		edgeSwitchMap.put(IPv4Address.of("10.10.1.2"), new Pair<DatapathId, OFPort>(DatapathId.of(0x101), OFPort.of(4)));
		edgeSwitchMap.put(IPv4Address.of("10.10.1.3"), new Pair<DatapathId, OFPort>(DatapathId.of(0x102), OFPort.of(3)));
		edgeSwitchMap.put(IPv4Address.of("10.10.1.4"), new Pair<DatapathId, OFPort>(DatapathId.of(0x102), OFPort.of(4)));
		edgeSwitchMap.put(IPv4Address.of("10.10.2.1"), new Pair<DatapathId, OFPort>(DatapathId.of(0x201), OFPort.of(3)));
		edgeSwitchMap.put(IPv4Address.of("10.10.2.2"), new Pair<DatapathId, OFPort>(DatapathId.of(0x201), OFPort.of(4)));
		edgeSwitchMap.put(IPv4Address.of("10.10.2.3"), new Pair<DatapathId, OFPort>(DatapathId.of(0x202), OFPort.of(3)));
		edgeSwitchMap.put(IPv4Address.of("10.10.2.4"), new Pair<DatapathId, OFPort>(DatapathId.of(0x202), OFPort.of(4)));
	}
}
