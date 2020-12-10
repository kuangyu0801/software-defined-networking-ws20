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
				Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
				if (eth.getEtherType() == EthType.IPv4) {
					IPv4 ipv4 = (IPv4) eth.getPayload();
					if (ipv4.getProtocol() == IpProtocol.UDP) {						
						logger.info("PACKET_IN message sent by switch: {}, IpProtocol:UDP"
								, sw.getId().toString());
						processPacketOutMessage(eth);
						
						if (UPDATE == false) {
							withoutUpdate();
							UPDATE = true;
						} else {
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
		// the current worable version is 1.4
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);

		// Matching
		// Input matching for port-1, -2, -3
		Match matchPort1 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(1))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		Match matchPort2 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(2))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		Match matchPort3 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(3))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		// Action & Output
		// action matching for port-1 , -2 ,-3, -4
		ArrayList<OFAction> actionListOutPort1 = new ArrayList<OFAction>();
		OFActions actions = myFactory.actions();
		OFActionOutput outputPort1 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(1))
				.build();
		actionListOutPort1.add(outputPort1);

		ArrayList<OFAction> actionListOutPort2 = new ArrayList<OFAction>();
		OFActionOutput outputPort2 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(2))
				.build();
		actionListOutPort2.add(outputPort2);

		ArrayList<OFAction> actionListOutPort3 = new ArrayList<OFAction>();
		OFActionOutput outputPort3 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(3))
				.build();
		actionListOutPort3.add(outputPort3);

		ArrayList<OFAction> actionListOutPort4 = new ArrayList<OFAction>();
		OFActionOutput outputPort4 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(4))
				.build();
		actionListOutPort4.add(outputPort4);

		// FlowAdd, with 10 time out
		OFFlowAdd flowAddPort1ToPort2 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setHardTimeout(10)
				.setMatch(matchPort1)
				.setActions(actionListOutPort2)
				.build();

		OFFlowAdd flowAddPort1ToPort3 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setHardTimeout(10)
				.setMatch(matchPort1)
				.setActions(actionListOutPort3)
				.build();

		OFFlowAdd flowAddPort3ToPort1 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setHardTimeout(10)
				.setMatch(matchPort3)
				.setActions(actionListOutPort1)
				.build();

		OFFlowAdd flowAddPort2ToPort4 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setHardTimeout(10)
				.setMatch(matchPort2)
				.setActions(actionListOutPort4)
				.build();

		// Set Switch
		// S1: port-1 to port-3
		switchService.getSwitch(DatapathId.of(1)).write(flowAddPort1ToPort3);
		switchService.getSwitch(DatapathId.of(3)).write(flowAddPort1ToPort2);
		switchService.getSwitch(DatapathId.of(4)).write(flowAddPort2ToPort4);
		switchService.getSwitch(DatapathId.of(7)).write(flowAddPort1ToPort3);
		switchService.getSwitch(DatapathId.of(8)).write(flowAddPort3ToPort1);

	}
	
	public void withUpdate(){
		// the current worable version is 1.4
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);

		// Matching
		// Input matching for port-1, -2
		Match matchPort1 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(1))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		Match matchPort2 = myFactory.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(2))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.build();

		// Action & Output
		// action matching for port-1 , -2 ,-3
		ArrayList<OFAction> actionListOutPort1 = new ArrayList<OFAction>();
		OFActions actions = myFactory.actions();
		OFActionOutput outputPort1 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(1))
				.build();
		actionListOutPort1.add(outputPort1);

		ArrayList<OFAction> actionListOutPort2 = new ArrayList<OFAction>();
		OFActionOutput outputPort2 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(2))
				.build();
		actionListOutPort2.add(outputPort2);

		ArrayList<OFAction> actionListOutPort3 = new ArrayList<OFAction>();
		OFActionOutput outputPort3 = actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(3))
				.build();
		actionListOutPort3.add(outputPort3);

		// FlowAdd without timeout
		OFFlowAdd flowAddPort1ToPort2 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setMatch(matchPort1)
				.setActions(actionListOutPort2)
				.build();

		OFFlowAdd flowAddPort1ToPort3 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setMatch(matchPort1)
				.setActions(actionListOutPort3)
				.build();

		OFFlowAdd flowAddPort2ToPort1 = myFactory.buildFlowAdd()
				.setPriority(1)
				.setMatch(matchPort2)
				.setActions(actionListOutPort1)
				.build();

		// Set Switch
		// S1: port-1 to port-3
		switchService.getSwitch(DatapathId.of(1)).write(flowAddPort1ToPort2);
		switchService.getSwitch(DatapathId.of(2)).write(flowAddPort1ToPort2);
		switchService.getSwitch(DatapathId.of(4)).write(flowAddPort1ToPort3);
		switchService.getSwitch(DatapathId.of(6)).write(flowAddPort1ToPort3);
		switchService.getSwitch(DatapathId.of(8)).write(flowAddPort2ToPort1);
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
