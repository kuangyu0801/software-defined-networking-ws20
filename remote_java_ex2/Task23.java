package net.sdnlab.ex2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionPopVlan;
import org.projectfloodlight.openflow.protocol.action.OFActionPushVlan;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;

public class Task23 implements IFloodlightModule, IOFMessageListener {
	// Since we are listening to OpenFlow messages we need to register with the FloodlightProvider (IFloodlightProviderService class)
	protected IFloodlightProviderService floodlightProvider; 
	protected IOFSwitchService switchService;
	// TODO: export logger
	// DONE: add back the logger
	// Finally, we need a logger to output what we've seen.
	protected static Logger logger;
	protected boolean UPDATE = false;


	// put in an ID for our OFMessage listener
	@Override
	public String getName() {
		// DONE Auto-generated method stub
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
//				logger.info("PACKET_IN message sent by switch: {}, getClass: {}"
//						,sw.getId().toString(),msg.getClass());
				Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
				if (eth.getEtherType() == EthType.IPv4) {
					IPv4 ipv4 = (IPv4) eth.getPayload();
					if (ipv4.getProtocol() == IpProtocol.UDP) {						
						logger.info("PACKET_IN message sent by switch: {}, IpProtocol:UDP"
								,sw.getId().toString());
						
						processPacketOutMessage(eth);
						
						if (UPDATE==false) {
							withoutUpdate();
							UPDATE=true;
						}else {
							withUpdate();
						}						
					}
				}
				break;
			default:
				break;
				
		}
		return Command.CONTINUE;
	}
	public void processPacketOutMessage(Ethernet eth){
		//send packet_out message out on port 1 of s8
		byte[] serializedData = eth.serialize();
		IOFSwitch s8 = switchService.getSwitch(DatapathId.of(8));
		OFPacketOut po = s8.getOFFactory().buildPacketOut()
				.setData(serializedData)
				.setActions(Collections.singletonList((OFAction) s8.getOFFactory().actions().output(OFPort.of(1),0xffFFffFF)))
				.setInPort(OFPort.CONTROLLER)
				.build();
		s8.write(po);
	}
	
	public void withoutUpdate(){
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);
		
		//S1
		Match myMatch_s1 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(1))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		ArrayList<OFAction> myActionList_s1 = new ArrayList<OFAction>();
		OFActions actions = myFactory.actions();
		OFActionOutput output_s1 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(3))
				.build();
		myActionList_s1.add(output_s1);

		OFFlowAdd flowAdd_s1 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setHardTimeout(10)
				.setMatch(myMatch_s1)
				.setActions(myActionList_s1)
				.build();
		switchService.getSwitch(DatapathId.of(1)).write(flowAdd_s1);

		//s3
		Match myMatch_s3 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(1))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		ArrayList<OFAction> myActionList_s3 = new ArrayList<OFAction>();
		OFActionOutput output_s3 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(2))
				.build();
		myActionList_s3.add(output_s3);

		OFFlowAdd flowAdd_s3 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setHardTimeout(10)
				.setMatch(myMatch_s3)
				.setActions(myActionList_s3)
				.build();
		switchService.getSwitch(DatapathId.of(3)).write(flowAdd_s3);

		//s4
		Match myMatch_s4 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(2))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		ArrayList<OFAction> myActionList_s4 = new ArrayList<OFAction>();
		OFActionOutput output_s4 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(4))
				.build();
		myActionList_s4.add(output_s4);

		OFFlowAdd flowAdd_s4 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setHardTimeout(10)
				.setMatch(myMatch_s4)
				.setActions(myActionList_s4)
				.build();
		switchService.getSwitch(DatapathId.of(4)).write(flowAdd_s4);

		//	S7 in:1, out 3
		Match myMatch_s7 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(1))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		ArrayList<OFAction> myActionList_s7 = new ArrayList<OFAction>();
		OFActionOutput output_s7 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(3))
				.build();
		myActionList_s7.add(output_s7);

		OFFlowAdd flowAdd_s7 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setHardTimeout(10)
				.setMatch(myMatch_s7)
				.setActions(myActionList_s7)
				.build();
		switchService.getSwitch(DatapathId.of(7)).write(flowAdd_s7);

		//S8 in:3 , out: 1
		Match myMatch_s8 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(3))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		ArrayList<OFAction> myActionList_s8 = new ArrayList<OFAction>();
		OFActionOutput output_s8 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(1))
				.build();
		myActionList_s8.add(output_s8);

		OFFlowAdd flowAdd_s8 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setHardTimeout(10)
				.setMatch(myMatch_s8)
				.setActions(myActionList_s8)
				.build();
		switchService.getSwitch(DatapathId.of(8)).write(flowAdd_s8);

	}
	
	public void withUpdate(){
		//s1
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);
		Match myMatch_s1 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(1))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		ArrayList<OFAction> myActionList_s1 = new ArrayList<OFAction>();
		OFActions actions = myFactory.actions();
		OFActionPushVlan pushVlan = actions.pushVlan(EthType.of(0x8100));
		myActionList_s1.add(pushVlan);

		OFOxms oxms = myFactory.oxms();
		OFActionSetField vlanid = actions.buildSetField()
				.setField(oxms.buildVlanVid().setValue(OFVlanVidMatch.ofVlan(10)).build())
				.build();
		myActionList_s1.add(vlanid);

		OFActionOutput output_s1 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(2))
				.build();
		myActionList_s1.add(output_s1);

		OFFlowAdd flowAdd_s1 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setMatch(myMatch_s1)
				.setActions(myActionList_s1)
				.build();
		switchService.getSwitch(DatapathId.of(1)).write(flowAdd_s1);
		//s2
		Match myMatch_s2 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(1))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlan(10))
				.build();

		ArrayList<OFAction> myActionList_s2 = new ArrayList<OFAction>();
		OFActionOutput output_s2 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(2))
				.build();
		myActionList_s2.add(output_s2);

		OFFlowAdd flowAdd_s2 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setMatch(myMatch_s2)
				.setActions(myActionList_s2)
				.build();
		switchService.getSwitch(DatapathId.of(2)).write(flowAdd_s2);
		//s4
		Match myMatch_s4 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(1))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlan(10))
				.build();

		ArrayList<OFAction> myActionList_s4 = new ArrayList<OFAction>();
		OFActionOutput output_s4 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(3))
				.build();
		myActionList_s4.add(output_s4);

		OFFlowAdd flowAdd_s4 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setMatch(myMatch_s4)
				.setActions(myActionList_s4)
				.build();
		switchService.getSwitch(DatapathId.of(4)).write(flowAdd_s4);
		//s6
		Match myMatch_s6 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(1))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlan(10))
				.build();

		ArrayList<OFAction> myActionList_s6 = new ArrayList<OFAction>();
		OFActionOutput output_s6 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(3))
				.build();
		myActionList_s6.add(output_s6);

		OFFlowAdd flowAdd_s6 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setMatch(myMatch_s6)
				.setActions(myActionList_s6)
				.build();
		switchService.getSwitch(DatapathId.of(6)).write(flowAdd_s6);
		//s8
		Match myMatch_s8 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(2))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlan(10))
				.build();

		ArrayList<OFAction> myActionList_s8 = new ArrayList<OFAction>();
		OFActionPopVlan popVlan = actions.popVlan();
		myActionList_s8.add(popVlan);

		OFActionOutput output_s8 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(1))
				.build();
		myActionList_s8.add(output_s8);

		OFFlowAdd flowAdd_s8 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setMatch(myMatch_s8)
				.setActions(myActionList_s8)
				.build();
		switchService.getSwitch(DatapathId.of(8)).write(flowAdd_s8);

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

	// we need to wire it up to the module loading system. We tell the module loader we depend on
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// DONE Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}
	// Init is called early in the controller startup process — it primarily is run to load dependencies and initialize datastructures.
	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// DONE Auto-generated method stub
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		// DONE: enable logger
		logger = LoggerFactory.getLogger(Task23.class);

	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// DONE Auto-generated method stub
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}

}
