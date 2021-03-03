package net.sdnlab.ex3.task32;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
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
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.routing.BroadcastTree;

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

	private boolean isGraphCreated;
	private Set<DatapathId> dpidSet;
	private Map<DatapathId, Set<Link>> switchLinkMap;
	// to look up host ip with its connecting edge switch dpid and port
	private Map<IPv4Address, Pair<DatapathId, OFPort>> hostEdgeSwitchMap;
	// to look up shortest path with root Minimum spanning tree of that root
	private Map<DatapathId, BroadcastTree> rootMstMap = new HashMap<>();;
	private Map<Link,Integer> linkCostMap;
	
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

	private void injectMessage(Ethernet eth) {
		logger.info("Inject message from controller");
		IPv4 ipv4 = (IPv4) eth.getPayload();
		IPv4Address dstAddress = ipv4.getDestinationAddress();
		Pair<DatapathId, OFPort> pair = hostEdgeSwitchMap.get(dstAddress);
		DatapathId dstDpid = pair.getKey();
		OFPort dstPort = pair.getValue();
		IOFSwitch dstSw = switchService.getSwitch(dstDpid);
		byte[] serializedData = eth.serialize();

		OFPacketOut po = dstSw.getOFFactory().buildPacketOut()
				.setData(serializedData)
				.setActions(Collections.singletonList((OFAction) dstSw.getOFFactory().actions().output(dstPort, 0xffFFffFF)))
				.setInPort(OFPort.CONTROLLER)
				.build();
		dstSw.write(po);
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
	


	// DONE: STEP-1: link state detection: create directed graph for djikstra
	private void createNetworkGraph() {
		logger.info("Get all DatapathID");
		dpidSet = switchService.getAllSwitchDpids();
		logger.info("Total switch number: " + dpidSet.size());
		logger.info("Getting switch-link map ");
		switchLinkMap = linkDiscoverer.getSwitchLinks();
		logger.info("Current switch links map size: " + switchLinkMap.size());
	}

	// DONE: STEP-2: calculate shortest path: using dijkstra
	private void calShortestPath(DatapathId srcDpid) {
		logger.info("calculated shortest path with switch: " + srcDpid.toString());
		// In Task3.2, the topology is static, only need to calculate link cost map once
		// But for generality, for both Task3.3, we recalculated every time
		linkCostMap = initLinkCostMap(HOPCOUNT, switchLinkMap);
		BroadcastTree broadcastTree = dijkstra(srcDpid, switchLinkMap, linkCostMap);
		rootMstMap.put(srcDpid, broadcastTree);
	}

	// DONE: STEP-3: create flow
	// return the shortest route from src to dst
	private List<Link> findFlow(DatapathId src, DatapathId dst) {
		logger.info("find flow for src: " + src.toString() + " dst: " + dst.toString());
		List<Link> list = new ArrayList<>();
		BroadcastTree mst = rootMstMap.get(src);
		// trace backward for the mst
		DatapathId next = dst;
		while (!next.equals(src)) {
			Link link = mst.getLinks().get(next);
			list.add(link);
			next = link.getSrc();
		}
		return list;
	}

	private void installOnSwitch(IPv4Address dstAddr, DatapathId srcDpid, OFPort srcOFPort) {
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);
		// set the match field of destination IP
		Match match = myFactory.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IPV4_DST, dstAddr)
				.build();
		// set actions of output flow of src output
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActionOutput output = myFactory.actions().buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(srcOFPort)
				.build();
		actionList.add(output);
		//add a flow entry
		OFFlowAdd flowAdd = myFactory.buildFlowAdd()
				.setPriority(1)
				.setMatch(match)
				.setActions(actionList)
				.build();
		// set src switch
		switchService.getSwitch(srcDpid).write(flowAdd);
	}

	// DONE: STEP-4: install flow when PACKET_IN event (reactivly)
	private void installFlow(IPv4Address srcAddr, IPv4Address dstAddr, List<Link> list) {
		// install all links one by one
		logger.info("Install flow src: " + srcAddr.toString() + " dst: " + dstAddr.toString());
		for (Link link : list) {
			logger.info("link src: " + link.getSrc().toString() + " dst: " + link.getDst().toString());
			installOnSwitch(dstAddr, link.getSrc(), link.getSrcPort());
		}
	}

	private void installEdgeSwitchToHost() {
		logger.info("Install all edge switches with host");
		for (IPv4Address hostAddr : hostEdgeSwitchMap.keySet()) {
			Pair<DatapathId, OFPort> pair = hostEdgeSwitchMap.get(hostAddr);
			installOnSwitch(hostAddr, pair.getKey(), pair.getValue());
		}
	}

	/*
	 * Creates a map of links and the cost associated with each link
	 *  Reference implementation: https://github.com/floodlight/floodlight/blob/d737cb05656a6038f4e2277ffb4503d45b7b29cb/src/main/java/net/floodlightcontroller/topology/TopologyInstance.java#L657
	 * */
	public Map<Link,Integer> initLinkCostMap(int type, Map<DatapathId, Set<Link>> links) {
		Map<Link, Integer> linkCost = new HashMap<Link, Integer>();
		//int tunnel_weight = portsWithLinks.size() + 1;

		switch (type){
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
			default:
				logger.info("Invalid Selection: Using Default Hop Count with Tunnel Bias for Metrics");
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

			// link is directional and {key = s1 : value {(s1-s2), (s2-s1)}
			for (Link link : links.get(curDpid)) {
				DatapathId neighbor = link.getDst();

				// ignore current swith is destination of link
				if (neighbor.equals(curDpid)) {
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

	// hard code associate all the host with their ingress switch
	public void initHostEdgeSwitch() {
		//Set up the routing table for first PACKET_IN inject
		logger.info("Init Host Edge Switch");
		hostEdgeSwitchMap = new HashMap<>();
		hostEdgeSwitchMap.put(IPv4Address.of("10.0.1.1"), new Pair<DatapathId, OFPort>(DatapathId.of(0x101), OFPort.of(3)));
		hostEdgeSwitchMap.put(IPv4Address.of("10.0.1.2"), new Pair<DatapathId, OFPort>(DatapathId.of(0x101), OFPort.of(4)));
		hostEdgeSwitchMap.put(IPv4Address.of("10.0.1.3"), new Pair<DatapathId, OFPort>(DatapathId.of(0x102), OFPort.of(3)));
		hostEdgeSwitchMap.put(IPv4Address.of("10.0.1.4"), new Pair<DatapathId, OFPort>(DatapathId.of(0x102), OFPort.of(4)));
		hostEdgeSwitchMap.put(IPv4Address.of("10.0.2.1"), new Pair<DatapathId, OFPort>(DatapathId.of(0x201), OFPort.of(3)));
		hostEdgeSwitchMap.put(IPv4Address.of("10.0.2.2"), new Pair<DatapathId, OFPort>(DatapathId.of(0x201), OFPort.of(4)));
		hostEdgeSwitchMap.put(IPv4Address.of("10.0.2.3"), new Pair<DatapathId, OFPort>(DatapathId.of(0x202), OFPort.of(3)));
		hostEdgeSwitchMap.put(IPv4Address.of("10.0.2.4"), new Pair<DatapathId, OFPort>(DatapathId.of(0x202), OFPort.of(4)));
		for (IPv4Address addr : hostEdgeSwitchMap.keySet()) {
			logger.info(addr.toString());
		}
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		// create graph in receive(), because this is a "rather" steady static
		if (!isGraphCreated) {
			initHostEdgeSwitch();
			installEdgeSwitchToHost();
			createNetworkGraph();
			isGraphCreated = true;
		}
		//logger.info("receive() callback is invokedp1");
		switch(msg.getType()) {
			case PACKET_IN:
				//logger.info("PACKET_IN event happened");
				Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
				if (eth.getEtherType() == EthType.IPv4) {
					logger.info("IP packet received");
					IPv4 ipv4Pkt = (IPv4) eth.getPayload();
					IPv4Address srcAddr = ipv4Pkt.getSourceAddress();
					IPv4Address dstAddr = ipv4Pkt.getDestinationAddress();
					// retrieving the dpid of source switch
					logger.info("src ip: " + srcAddr.toString());
					logger.info("dst ip: " + dstAddr.toString());
					if (hostEdgeSwitchMap.containsKey(srcAddr)) {
						logger.info("hostEdgeSwitchMap has key");
					} else {
						logger.info("no key is found");
					}
					DatapathId srcDpid = hostEdgeSwitchMap.get(srcAddr).getKey();
					DatapathId dstDpid = hostEdgeSwitchMap.get(dstAddr).getKey();

					// mst has not be calculated
					if (!rootMstMap.containsKey(srcDpid)) {
						calShortestPath(srcDpid);
					}

					List<Link> linkList = findFlow(srcDpid, dstDpid);
					installFlow(srcAddr, dstAddr, linkList);
					// Inject the first packet directly at the target switch
					injectMessage(eth);
				}
				break;
			default:
				break;
		}
		return Command.CONTINUE;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// DONE Auto-generated method stub
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		linkDiscoverer = context.getServiceImpl(ILinkDiscoveryService.class);
		setupLogger();
		isGraphCreated = false;
		logger.info("Init");
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// DONE Auto-generated method stub
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		logger.info("Start Up");

		logger.info("Start Up End");
	}
}
