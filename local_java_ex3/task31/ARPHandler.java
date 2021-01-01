package net.sdnlab.ex3.task31;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

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
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;

public class ARPHandler implements IFloodlightModule, IOFMessageListener {

	private static final Logger logger = Logger.getLogger(ARPHandler.class.getSimpleName());

	protected IFloodlightProviderService floodlightProvider;
	protected IOFSwitchService switchService;
	private boolean isInstalled = false;
	private Map<IPv4Address, MacAddress> mapCentralArpCache;
	private Map<DatapathId, Map<IPv4Address, OFPort>> mapRoutingTables;
	private Map<IPv4Address, DatapathId> mapHostToSwitch;
	
	protected ILinkDiscoveryService linkDiscoverer;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		switch(msg.getType()) {
			case PACKET_IN:
				// install all ip flows in advance
//				if (!isInstalled) {					
//					for (DatapathId dpid : switchService.getAllSwitchDpids()) {
//						logger.info("Links of switch" + dpid.toString());
//						for (Link link : linkDiscoverer.getSwitchLinks().get(dpid)) {
//							logger.info(link.toString());
//						}
//					}
//					isInstalled = true;
//				}
				
				Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
				if (eth.getEtherType() == EthType.ARP) {
					OFPacketIn piMsg = (OFPacketIn) msg;
					OFPort inPort = piMsg.getMatch().get(MatchField.IN_PORT);
					ARP arp = (ARP) eth.getPayload();
					if (arp.getOpCode() == ArpOpcode.REQUEST){
						logger.info ("Received ARP request in switch " + sw.getId() + " by port " + inPort);

						if (!mapCentralArpCache.containsKey(arp.getSenderProtocolAddress())){
							//if source MAC has not been seen then store it in central ARP cache
							mapCentralArpCache.put(arp.getSenderProtocolAddress(), arp.getSenderHardwareAddress());
						}

						if (mapCentralArpCache.containsKey(arp.getTargetProtocolAddress())){
							//if internal ARP cache contains destination MAC, immediately inject appropriate reply
							sendARPReply(sw, inPort, arp);
						} else {
							//otherwise redirects the ARP request to the target host
							forwardMessage(eth, "ARP request");
						}

					} else if (arp.getOpCode() == ArpOpcode.REPLY){
						logger.info ("Received ARP reply in switch " + sw.getId() + " by port " + inPort);
						//save the reply to its internal ARP cache, before injecting reply
						mapCentralArpCache.put(arp.getSenderProtocolAddress(), arp.getSenderHardwareAddress());
						forwardMessage(eth, "ARP reply");
					}
				} else if (eth.getEtherType() == EthType.IPv4) {
					logger.info ("Received dropped IPv4 packet");
					installStaticEntries();
					
					IPv4 ipv4 = (IPv4) eth.getPayload();
					IPv4Address dstAddress = ipv4.getDestinationAddress();
					DatapathId dstDpid = mapHostToSwitch.get(dstAddress);
					OFPort dstPort = mapRoutingTables.get(dstDpid).get(dstAddress);
					IOFSwitch dstSw = switchService.getSwitch(dstDpid);		
					byte[] serializedData = eth.serialize();
					
					OFPacketOut po = dstSw.getOFFactory().buildPacketOut()
							.setData(serializedData)
							.setActions(Collections.singletonList((OFAction) dstSw.getOFFactory().actions().output(dstPort, 0xffFFffFF)))
							.setInPort(OFPort.CONTROLLER)
							.build();
					dstSw.write(po);
				}
			default:
				break;
		}
		return Command.CONTINUE;
	}

	// DONE: finish ARP reply
	public void sendARPReply(IOFSwitch sw, OFPort inPort, ARP arpRequest){
		logger.info ("Destination MAC address exists, directly send ARP reply to switch " + sw.getId() + " on port " + inPort);
		// Create an ARP reply frame (from target (source) to source (destination)).
		IPacket arpReply = new Ethernet()
				.setSourceMACAddress(mapCentralArpCache.get(arpRequest.getTargetProtocolAddress()))
				.setDestinationMACAddress(arpRequest.getSenderHardwareAddress())
				.setEtherType(EthType.ARP)
				.setPayload(new ARP()
						.setHardwareType(ARP.HW_TYPE_ETHERNET)
						.setProtocolType(ARP.PROTO_TYPE_IP)
						.setOpCode(ARP.OP_REPLY)
						.setHardwareAddressLength((byte)6)
						.setProtocolAddressLength((byte)4)
						.setSenderHardwareAddress(mapCentralArpCache.get(arpRequest.getTargetProtocolAddress()))
						.setSenderProtocolAddress(arpRequest.getTargetProtocolAddress())
						.setTargetHardwareAddress(arpRequest.getSenderHardwareAddress())
						.setTargetProtocolAddress(arpRequest.getSenderProtocolAddress())
						.setPayload(new Data(new byte[] {0x01})));
		// Send ARP reply.
		byte[] serializedData = arpReply.serialize();
		OFPacketOut po = sw.getOFFactory().buildPacketOut()
				.setData(serializedData)
				.setActions(Collections.singletonList((OFAction) sw.getOFFactory().actions().output(inPort,0xffFFffFF)))
				.setInPort(OFPort.CONTROLLER)
				.build();
		sw.write(po);
	}

	// DONE: finish this method
	public void forwardMessage(Ethernet eth, String arpOpcode){
		//Get corresponding switch and outport to forward the ARP request/reply
		ARP arp = (ARP) eth.getPayload();
		IPv4Address dstIPAddress = arp.getTargetProtocolAddress();
		DatapathId switchId = mapHostToSwitch.get(dstIPAddress);
		Map<IPv4Address, OFPort> routingTable = mapRoutingTables.get(switchId);
		OFPort outPort = routingTable.get(dstIPAddress);

		//Compose the Packet-out message and send it out
		byte[] serializedData = eth.serialize();
		IOFSwitch sw = switchService.getSwitch(switchId);
		OFPacketOut po = sw.getOFFactory().buildPacketOut()
				.setData(serializedData)
				.setActions(Collections.singletonList((OFAction) sw.getOFFactory().actions().output(outPort,0xffFFffFF)))
				.setInPort(OFPort.CONTROLLER)
				.build();
		sw.write(po);
		logger.info ("forward " + arpOpcode + " to switch " + switchId + " on port " + outPort);

	}

	public void installStaticEntries(){
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);
		// install static entries for each switch
		for(DatapathId switchId : mapRoutingTables.keySet()) {
			logger.info ("install flow entries in switch " + switchId.toString());
			Map<IPv4Address, OFPort> routingTable = mapRoutingTables.get(switchId);
			for(IPv4Address ipv4Address : routingTable.keySet()){
				OFPort outPort = routingTable.get(ipv4Address);
				//set the match field
				Match match = myFactory.buildMatch()
						.setExact(MatchField.ETH_TYPE, EthType.IPv4)
						.setExact(MatchField.IPV4_DST, ipv4Address)
						.build();
				//set actions
				ArrayList<OFAction> actionList = new ArrayList<OFAction>();
				OFActionOutput output = myFactory.actions().buildOutput()
						.setMaxLen(0xFFffFFff)
						.setPort(outPort)
						.build();
				actionList.add(output);
				//add a flow entry
				OFFlowAdd flowAdd = myFactory.buildFlowAdd()
						.setPriority(1)
						.setMatch(match)
						.setActions(actionList)
						.build();
				switchService.getSwitch(switchId).write(flowAdd);
			}
		}

	}

	public void setRoutingTables(){
		//Set up the routing table for S1
		Map<IPv4Address, OFPort> routingTableS1 = new HashMap<>();
		routingTableS1.put(IPv4Address.of("10.10.1.1"), OFPort.of(1));
		routingTableS1.put(IPv4Address.of("10.10.1.2"), OFPort.of(2));
		routingTableS1.put(IPv4Address.of("10.10.1.3"), OFPort.of(3));
		routingTableS1.put(IPv4Address.of("10.10.2.1"), OFPort.of(4));
		routingTableS1.put(IPv4Address.of("10.10.2.2"), OFPort.of(4));
		routingTableS1.put(IPv4Address.of("10.10.2.3"), OFPort.of(4));
		routingTableS1.put(IPv4Address.of("10.10.4.1"), OFPort.of(4));
		routingTableS1.put(IPv4Address.of("10.10.4.2"), OFPort.of(4));
		routingTableS1.put(IPv4Address.of("10.10.4.3"), OFPort.of(4));
		//Set up the routing table for S2
		Map<IPv4Address, OFPort> routingTableS2 = new HashMap<>();
		routingTableS2.put(IPv4Address.of("10.10.2.1"), OFPort.of(1));
		routingTableS2.put(IPv4Address.of("10.10.2.2"), OFPort.of(2));
		routingTableS2.put(IPv4Address.of("10.10.2.3"), OFPort.of(3));
		routingTableS2.put(IPv4Address.of("10.10.1.1"), OFPort.of(4));
		routingTableS2.put(IPv4Address.of("10.10.1.2"), OFPort.of(4));
		routingTableS2.put(IPv4Address.of("10.10.1.3"), OFPort.of(4));
		routingTableS2.put(IPv4Address.of("10.10.4.1"), OFPort.of(5));
		routingTableS2.put(IPv4Address.of("10.10.4.2"), OFPort.of(5));
		routingTableS2.put(IPv4Address.of("10.10.4.3"), OFPort.of(5));
		//Set up the routing table for S3
		Map<IPv4Address, OFPort> routingTableS3 = new HashMap<>();
		routingTableS3.put(IPv4Address.of("10.10.1.1"), OFPort.of(1));
		routingTableS3.put(IPv4Address.of("10.10.1.2"), OFPort.of(1));
		routingTableS3.put(IPv4Address.of("10.10.1.3"), OFPort.of(1));
		routingTableS3.put(IPv4Address.of("10.10.2.1"), OFPort.of(1));
		routingTableS3.put(IPv4Address.of("10.10.2.2"), OFPort.of(1));
		routingTableS3.put(IPv4Address.of("10.10.2.3"), OFPort.of(1));
		routingTableS3.put(IPv4Address.of("10.10.4.1"), OFPort.of(2));
		routingTableS3.put(IPv4Address.of("10.10.4.2"), OFPort.of(2));
		routingTableS3.put(IPv4Address.of("10.10.4.3"), OFPort.of(2));
		//Set up the routing table for S4
		Map<IPv4Address, OFPort> routingTableS4 = new HashMap<>();
		routingTableS4.put(IPv4Address.of("10.10.4.1"), OFPort.of(1));
		routingTableS4.put(IPv4Address.of("10.10.4.2"), OFPort.of(2));
		routingTableS4.put(IPv4Address.of("10.10.4.3"), OFPort.of(3));
		routingTableS4.put(IPv4Address.of("10.10.1.1"), OFPort.of(4));
		routingTableS4.put(IPv4Address.of("10.10.1.2"), OFPort.of(4));
		routingTableS4.put(IPv4Address.of("10.10.1.3"), OFPort.of(4));
		routingTableS4.put(IPv4Address.of("10.10.2.1"), OFPort.of(4));
		routingTableS4.put(IPv4Address.of("10.10.2.2"), OFPort.of(4));
		routingTableS4.put(IPv4Address.of("10.10.2.3"), OFPort.of(4));


		mapRoutingTables = new HashMap<DatapathId, Map<IPv4Address, OFPort>>();
		mapRoutingTables.put(DatapathId.of(1),routingTableS1);
		mapRoutingTables.put(DatapathId.of(2),routingTableS2);
		mapRoutingTables.put(DatapathId.of(3),routingTableS3);
		mapRoutingTables.put(DatapathId.of(4),routingTableS4);

		//Map each host to the corresponding switch
		mapHostToSwitch = new HashMap<IPv4Address, DatapathId>();
		mapHostToSwitch.put(IPv4Address.of("10.10.1.1"), DatapathId.of(1));
		mapHostToSwitch.put(IPv4Address.of("10.10.1.2"), DatapathId.of(1));
		mapHostToSwitch.put(IPv4Address.of("10.10.1.3"), DatapathId.of(1));
		mapHostToSwitch.put(IPv4Address.of("10.10.2.1"), DatapathId.of(2));
		mapHostToSwitch.put(IPv4Address.of("10.10.2.2"), DatapathId.of(2));
		mapHostToSwitch.put(IPv4Address.of("10.10.2.3"), DatapathId.of(2));
		mapHostToSwitch.put(IPv4Address.of("10.10.4.1"), DatapathId.of(4));
		mapHostToSwitch.put(IPv4Address.of("10.10.4.2"), DatapathId.of(4));
		mapHostToSwitch.put(IPv4Address.of("10.10.4.3"), DatapathId.of(4));

	}




		@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// DONE Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IOFSwitchService.class);
		l.add(ILinkDiscoveryService.class);
		return l;
	}

	private void setupLogger() {
		// DONE: export logger output into log file
		try {
			FileHandler fileHandler = new FileHandler("/home/student/ex3/task31.log");
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
		mapCentralArpCache = new HashMap<>();
		setRoutingTables();
		setupLogger();
		logger.info("Init");
		linkDiscoverer = context.getServiceImpl(ILinkDiscoveryService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// DONE Auto-generated method stub
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		logger.info("Start Up");
	}

}
