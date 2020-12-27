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
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
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
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;

public class ARPHandler implements IFloodlightModule, IOFMessageListener {
	
	private static final Logger logger = Logger.getLogger(ARPHandler.class.getSimpleName());
	
	protected IFloodlightProviderService floodlightProvider;
	protected IOFSwitchService switchService;
	private Map<IPv4Address, MacAddress> centralArpCache;
	private Map<IPv4AddressWithMask, OFPort> routingTableS1;
	private Map<IPv4AddressWithMask, OFPort> routingTableS2;
	private Map<IPv4AddressWithMask, OFPort> routingTableS3;
	private Map<IPv4AddressWithMask, OFPort> routingTableS4;


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
				OFPacketIn piMsg = (OFPacketIn) msg;
				OFPort inPort = piMsg.getInPort();
				Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
				if (eth.getEtherType() == EthType.ARP) {
					ARP arp = (ARP) eth.getPayload();
					if (arp.getOpCode() == ArpOpcode.REQUEST){
						logger.info ("Received ARP request in switch "+sw.getId()+" by port "+inPort);

						IPv4Address sourceIPAddress = arp.getSenderProtocolAddress();
						if (!centralArpCache.containsKey(sourceIPAddress)){
							//get source MAC and store it in central ARP cache
							MacAddress sourceMACAddress = arp.getSenderHardwareAddress();
							centralArpCache.put(sourceIPAddress, sourceMACAddress);
						}

						//Controller queries internal ARP cache for MAC address
						IPv4Address targetIPAddress = arp.getTargetProtocolAddress();
						if (centralArpCache.containsKey(targetIPAddress)){
							//if internal ARP cahce contains arp_tha, immediately inject appropriate reply
							sendARPReply(sw, inPort, arp);
						}else{
							//otherwise redirects the ARP request to the target host and save the reply
							//to its internal ARP cache, before injecting reply
							forwardMessage(eth);
						}

					}else if (arp.getOpCode() == ArpOpcode.REPLY){
						//MacAddress sourceMACAddress = arp.getSenderHardwareAddress();
						forwardMessage(eth);
					}
				}else if (eth.getEtherType() == EthType.IPv4) {
					installStaticEntries();
				}
		default:
			break;
		}
		return null;
	}

	public void sendARPReply(IOFSwitch sw, OFPort inPort, ARP arpRequest){
		// Create an ARP reply frame (from target (source) to source (destination)).
		IPacket arpReply = new Ethernet()
				.setSourceMACAddress(centralArpCache.get(arpRequest.getTargetProtocolAddress()))
				.setDestinationMACAddress(arpRequest.getSenderHardwareAddress())
				.setEtherType(EthType.ARP)
				.setPayload(new ARP()
						.setHardwareType(ARP.HW_TYPE_ETHERNET)
						.setProtocolType(ARP.PROTO_TYPE_IP)
						.setOpCode(ARP.OP_REPLY)
						.setHardwareAddressLength((byte)6)
						.setProtocolAddressLength((byte)4)
						.setSenderHardwareAddress(centralArpCache.get(arpRequest.getTargetProtocolAddress()))
						.setSenderProtocolAddress(arpRequest.getTargetProtocolAddress())
						.setTargetHardwareAddress(arpRequest.getSenderHardwareAddress())
						.setTargetProtocolAddress(arpRequest.getSenderProtocolAddress())
						.setPayload(new Data(new byte[] {0x01})));
		// Send ARP reply.
		//sendPOMessage(arpReply, floodlightProvider.getSwitch(arpRequest.getSwitchId()), arpRequest.getInPort());


		byte[] serializedData = arpReply.serialize();
		OFPacketOut po = sw.getOFFactory().buildPacketOut()
				.setData(serializedData)
				.setActions(Collections.singletonList((OFAction) sw.getOFFactory().actions().output(inPort,0xffFFffFF)))
				.setInPort(OFPort.CONTROLLER)
				.build();
		sw.write(po);
	}
	
	// TODO: finish this method
	public void forwardMessage(Ethernet eth){
		ARP arp = (ARP) eth.getPayload();
		IPv4Address targetIPAddress = arp.getTargetProtocolAddress();
		
		//OFPort outPort =
	}

	public void installStaticEntries(){
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);
		for(IPv4AddressWithMask ipv4AddressWithMask : routingTableS1.keySet()){
			OFPort outPort = routingTableS1.get(ipv4AddressWithMask);
			//set match field
			Match match = myFactory.buildMatch()
					.setExact(MatchField.ETH_TYPE, EthType.IPv4)
					.setMasked(MatchField.IPV4_DST, ipv4AddressWithMask)
					.build();
			//set actions
			ArrayList<OFAction> actionList = new ArrayList<OFAction>();
			OFActions actions = myFactory.actions();
			OFActionOutput outputPort1 = actions.buildOutput()
					.setMaxLen(0xFFffFFff)
					.setPort(outPort)
					.build();
			actionList.add(outputPort1);
			//add flow entry
			OFFlowAdd flowAdd = myFactory.buildFlowAdd()
					.setPriority(1)
					.setMatch(match)
					.setActions(actionList)
					.build();
			switchService.getSwitch(DatapathId.of(1)).write(flowAdd);
		}
	}

	public void setRoutingTables(){
		routingTableS1 = new HashMap<>();
		routingTableS1.put(IPv4AddressWithMask.of("10.10.1.1/8"), OFPort.of(1));
		routingTableS1.put(IPv4AddressWithMask.of("10.10.1.2/8"), OFPort.of(2));
		routingTableS1.put(IPv4AddressWithMask.of("10.10.1.3/8"), OFPort.of(3));
		routingTableS1.put(IPv4AddressWithMask.of("10.10.2.1/8"), OFPort.of(4));
		routingTableS1.put(IPv4AddressWithMask.of("10.10.2.2/8"), OFPort.of(4));
		routingTableS1.put(IPv4AddressWithMask.of("10.10.2.3/8"), OFPort.of(4));
		routingTableS1.put(IPv4AddressWithMask.of("10.10.4.1/8"), OFPort.of(4));
		routingTableS1.put(IPv4AddressWithMask.of("10.10.4.2/8"), OFPort.of(4));
		routingTableS1.put(IPv4AddressWithMask.of("10.10.4.3/8"), OFPort.of(4));

		routingTableS2 = new HashMap<>();
		routingTableS2.put(IPv4AddressWithMask.of("10.10.2.1/8"), OFPort.of(1));
		routingTableS2.put(IPv4AddressWithMask.of("10.10.2.2/8"), OFPort.of(2));
		routingTableS2.put(IPv4AddressWithMask.of("10.10.2.3/8"), OFPort.of(3));
		routingTableS2.put(IPv4AddressWithMask.of("10.10.1.1/8"), OFPort.of(4));
		routingTableS2.put(IPv4AddressWithMask.of("10.10.1.2/8"), OFPort.of(4));
		routingTableS2.put(IPv4AddressWithMask.of("10.10.1.3/8"), OFPort.of(4));
		routingTableS2.put(IPv4AddressWithMask.of("10.10.4.1/8"), OFPort.of(5));
		routingTableS2.put(IPv4AddressWithMask.of("10.10.4.2/8"), OFPort.of(5));
		routingTableS2.put(IPv4AddressWithMask.of("10.10.4.3/8"), OFPort.of(5));

		routingTableS3 = new HashMap<>();
		routingTableS3.put(IPv4AddressWithMask.of("10.10.1.1/8"), OFPort.of(1));
		routingTableS3.put(IPv4AddressWithMask.of("10.10.1.2/8"), OFPort.of(1));
		routingTableS3.put(IPv4AddressWithMask.of("10.10.1.3/8"), OFPort.of(1));
		routingTableS3.put(IPv4AddressWithMask.of("10.10.2.1/8"), OFPort.of(1));
		routingTableS3.put(IPv4AddressWithMask.of("10.10.2.2/8"), OFPort.of(1));
		routingTableS3.put(IPv4AddressWithMask.of("10.10.2.3/8"), OFPort.of(1));
		routingTableS3.put(IPv4AddressWithMask.of("10.10.4.1/8"), OFPort.of(2));
		routingTableS3.put(IPv4AddressWithMask.of("10.10.4.2/8"), OFPort.of(2));
		routingTableS3.put(IPv4AddressWithMask.of("10.10.4.3/8"), OFPort.of(2));

		routingTableS4 = new HashMap<>();
		routingTableS4.put(IPv4AddressWithMask.of("10.10.4.1/8"), OFPort.of(1));
		routingTableS4.put(IPv4AddressWithMask.of("10.10.4.2/8"), OFPort.of(2));
		routingTableS4.put(IPv4AddressWithMask.of("10.10.4.3/8"), OFPort.of(3));
		routingTableS4.put(IPv4AddressWithMask.of("10.10.1.1/8"), OFPort.of(4));
		routingTableS4.put(IPv4AddressWithMask.of("10.10.1.2/8"), OFPort.of(4));
		routingTableS4.put(IPv4AddressWithMask.of("10.10.1.3/8"), OFPort.of(4));
		routingTableS4.put(IPv4AddressWithMask.of("10.10.2.1/8"), OFPort.of(4));
		routingTableS4.put(IPv4AddressWithMask.of("10.10.2.2/8"), OFPort.of(4));
		routingTableS4.put(IPv4AddressWithMask.of("10.10.2.3/8"), OFPort.of(4));

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
		// TODO Auto-generated method stub
		return null;
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
		centralArpCache = new HashMap<>();
		setupLogger();
		logger.info("Init");
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// DONE Auto-generated method stub
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		logger.info("Start Up");
	}

}
