package net.sdnlab.ex4.task43;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowDelete;
import org.projectfloodlight.openflow.protocol.OFFlowDeleteStrict;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.util.FlowModUtils;

public class Task43 implements IFloodlightModule, ITask43Service {

	protected IFloodlightProviderService floodlightProvider;
	protected IRestApiService restApiService;
	protected IOFSwitchService switchService;
	private Map<IPv4Address,OFPort> ipToSwPortMap;

	// TODO: add any other required services

	protected static Logger logger;
	
	public static class Columns {
		public static final String COLUMN_UDP_PORT = "udp_port";
		public static final String COLUMN_TYPE = "type";
		public static final String COLUMN_REFERENCE_VALUE = "reference_value";
		public static final String COLUMN_FILTER_ENALBE = "filter_enable";
		public static final String COLUMN_IS_GREATER = "is_greater";
	}
	
	private Map<String, Subscription> subMap = new HashMap<>();
	private List<Subscription> [] lessListArr = new List[]{new ArrayList<>(), new ArrayList<>()}; // 0:type-0, 1:type-1
	private List<Subscription> [] greaterListArr = new List[]{new ArrayList<>(), new ArrayList<>()}; // new List<Subscription>[]{new ArrayList<>(), new ArrayList<>()}; // 0:type-0, 1:type-1
	private Map<IOFSwitch, List<OFFlowAdd>>[] lessDelMapArr = new Map[]{new HashMap<>(), new HashMap<>()}; // 0:type-0, 1:type-1
	private Map<IOFSwitch, List<OFFlowAdd>>[] greaterDelMapArr = new Map[]{new HashMap<>(), new HashMap<>()}; // 0:type-0, 1:type-1

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> services = new ArrayList<Class<? extends IFloodlightService>>();
		services.add(ITask43Service.class);
		return services;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> impls = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		impls.put(ITask43Service.class, this);
		return impls;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> deps = new ArrayList<Class<? extends IFloodlightService>>();
		deps.add(IFloodlightProviderService.class);
		deps.add(IRestApiService.class);
		// TODO: add any other required service dependencies
		deps.add(IOFSwitchService.class);
		return deps;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);

		// TODO: initialize any other required services
		switchService = context.getServiceImpl(IOFSwitchService.class);
		
		logger = LoggerFactory.getLogger(Task43.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		restApiService.addRestletRoutable(new Task43WebRoutable());
		setIpTable();
	}

	@Override
	public String listSubscriptions() {
		logger.info("Listing all subscriptions"); // you may change logging to your liking
		String jsonSubscriptions = "{}";

		// DONE Implement!
		System.out.println("receive a GET Request");
		JsonFactory f = new JsonFactory(); // may alternatively construct directly too

		// First: write simple JSON output
		StringWriter sw = new StringWriter();
		JsonGenerator jGen;
		try {
			jGen = f.createGenerator(sw);			
			jGen.writeStartObject();
			if (subMap.keySet() != null) {
				for (String name: subMap.keySet()) {
					// TODO: convert all types to String
					if (subMap.get(name) != null) {
						Subscription sub = subMap.get(name);
						logger.info("Pringting subscription:" + name);
						jGen.writeArrayFieldStart(name);
						jGen.writeStartObject();
						jGen.writeNumberField(Task43.Columns.COLUMN_UDP_PORT, sub.getUdpPort());
						jGen.writeBooleanField(Task43.Columns.COLUMN_FILTER_ENALBE, sub.isFiltered());
						jGen.writeNumberField(Task43.Columns.COLUMN_TYPE, sub.getType());
						jGen.writeNumberField(Task43.Columns.COLUMN_REFERENCE_VALUE, sub.getrVal());
						jGen.writeBooleanField(Task43.Columns.COLUMN_IS_GREATER, sub.isGreater());
						jGen.writeEndObject();
						jGen.writeEndArray();
					}
				}
			}
			jGen.writeEndObject();
			jGen.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jsonSubscriptions = sw.toString();
		return jsonSubscriptions;
	}

	@Override
	public String addSubscription(String name /* DONE: add arguments */, Subscription sub) {
		logger.info("Adding subscription " + name); // you may change logging to your liking
		String status;

		// DONE Implement!
		System.out.println("Post Request from: "+name);
		if (subMap.containsKey(name)) {
			status = "Error! Subscription " + name + " already exists";
		} else {
			status = "Successfully added new subscription " + name;
			subMap.put(name, sub);
			// TODO: change method call to init
			setIpTable();
			installFlow(name, sub);
		}
		 
		return "{\"status\":\"" + status + "\"}";
	}
	
	private void deleteFlow(Map<IOFSwitch, List<OFFlowAdd>> deleteMap) {
		logger.info("delete content-based flows");	
		if(!deleteMap.isEmpty()) {
			for(IOFSwitch sw: deleteMap.keySet()) {
				if(!sw.getId().equals(DatapathId.of(3))) {
					List<OFFlowAdd> deleteList = deleteMap.get(sw);
					for(OFFlowAdd flowAdd: deleteList) {
						OFFlowDeleteStrict flowDelete = FlowModUtils.toFlowDeleteStrict(flowAdd);
						logger.info("delete flow: "+flowDelete.toString());	
						sw.write(flowDelete);
					}
				}else if(subMap.isEmpty()) {
					// delete flow on S3 when there is no subscription
					List<OFFlowAdd> deleteList = deleteMap.get(sw);
					for(OFFlowAdd flowAdd: deleteList) {
						OFFlowDeleteStrict flowDelete = FlowModUtils.toFlowDeleteStrict(flowAdd);
						logger.info("delete flow on S3: "+flowDelete.toString());	
						sw.write(flowDelete);
					}
				}
			}
		deleteMap.clear();
		}
	}

	// TODO: complete method, refactor and combine case to get rid of if-else
	private void installFlow(String name, Subscription sub) {

		// classify the subscriptions according to the type and comparator
		Map<IOFSwitch, List<OFFlowAdd>> delMap = (sub.isGreater()) ? greaterDelMapArr[sub.getType()] : lessDelMapArr[sub.getType()];
		List<Subscription> installList = (sub.isGreater()) ? greaterListArr[sub.getType()] : lessListArr[sub.getType()];
		logger.info("install for "+name+" isGreater: "+sub.isGreater() + " value: "+sub.getrVal()+" type: " + sub.getType());
			installList.add(sub);
			deleteFlow(delMap);
			if (sub.isGreater()) {
				installGreater(installList, delMap);
			} else {
				installLessEqual(installList, delMap);
			}
			
	}

	private void installGreater(List<Subscription> greaterList, Map<IOFSwitch, List<OFFlowAdd>> deleteMap){
		//Sorts the subscription list into ascending order, according to the reference value
		Collections.sort(greaterList);
		int priority = 100;
		List<Subscription> dstList = new ArrayList<Subscription>();
		List<OFFlowAdd> deleteS1List = new ArrayList<>();
		List<OFFlowAdd> deleteS2List = new ArrayList<>();
		List<OFFlowAdd> deleteS3List = new ArrayList<>();
		// need to install 1 addtional flow: rest flow (ex. > 100 need to install drop when  <= 63, and all pass flow
		for(int i = 0; i <= greaterList.size(); i += 1){
			if (i == 0){
				installOnS1(greaterList.get(0), priority,deleteS1List,deleteMap);
			} else {
				String ipv4AddressWithMask = (i == greaterList.size()) ? "230." + greaterList.get(i - 1).getType() + ".0.0/16"
						: greaterList.get(i).computeMask();
				dstList.add(greaterList.get(i - 1)); 
				installOnS2(ipv4AddressWithMask, priority, dstList, deleteS2List, deleteMap);
				priority -= 1;
			}
		}
		installOnS3(deleteS3List,deleteMap);
	}
	
	private void installLessEqual(List<Subscription> lessList, Map<IOFSwitch, List<OFFlowAdd>> deleteMap){
		//Sorts the subscription list into ascending order, according to the reference value
		Collections.sort(lessList);
		int priority = 500;
		List<Subscription> dstList = new ArrayList<Subscription>();
		List<OFFlowAdd> deleteS1List = new ArrayList<>();
		List<OFFlowAdd> deleteS2List = new ArrayList<>();
		List<OFFlowAdd> deleteS3List = new ArrayList<>();
		for (int i = lessList.size() - 1; i >= 0; i -= 1){
			// install only the largest upper bound on S1
			if (i == lessList.size() - 1) {
				installOnS1(lessList.get(i), priority,deleteS1List,deleteMap);
			}

			String ipv4AddressWithMask = lessList.get(i).computeMask();
			dstList.add(lessList.get(i));
			installOnS2(ipv4AddressWithMask, priority, dstList, deleteS2List, deleteMap);
			priority += 1;
		}
		installOnS3(deleteS3List,deleteMap);
	}

	public void installOnS1(Subscription sub, int priority, List<OFFlowAdd> deleteS1List, Map<IOFSwitch, List<OFFlowAdd>> deleteMap){
		logger.info("install flow entries on s1");
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);
		IOFSwitch s1 = switchService.getSwitch(DatapathId.of(1));
		Match match = myFactory.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setMasked(MatchField.IPV4_DST, IPv4AddressWithMask.of(sub.computeMask()))
				.build();
		if(sub.isGreater()){
			//">": set no action to drop the value below the reference value
			OFFlowAdd flowAdd = myFactory.buildFlowAdd()
					.setPriority(priority)
					.setMatch(match)
					.build();
			s1.write(flowAdd);
			deleteS1List.add(flowAdd);
			// let the rest pass
			Match match_rest = myFactory.buildMatch()
					.setExact(MatchField.ETH_TYPE, EthType.IPv4)
					.setMasked(MatchField.IPV4_DST, IPv4AddressWithMask.of("230."+sub.getType()+".0.0/16"))
					.build();
			ArrayList<OFAction> actionList = new ArrayList<OFAction>();
			OFActionOutput output = myFactory.actions().buildOutput()
					.setMaxLen(0xFFffFFff)
					.setPort(OFPort.of(1))
					.build();
			actionList.add(output);
			OFFlowAdd flowAdd_rest = myFactory.buildFlowAdd()
					.setPriority(priority-1)
					.setMatch(match_rest)
					.setActions(actionList)
					.build();
			s1.write(flowAdd_rest);
			deleteS1List.add(flowAdd_rest);
		}else{
			// "<=": forward
			ArrayList<OFAction> actionList = new ArrayList<OFAction>();
			OFActionOutput output = myFactory.actions().buildOutput()
					.setMaxLen(0xFFffFFff)
					.setPort(OFPort.of(1))
					.build();
			actionList.add(output);
			//add a flow entry
			OFFlowAdd flowAdd = myFactory.buildFlowAdd()
					.setPriority(priority)
					.setMatch(match)
					.setActions(actionList)
					.build();
			s1.write(flowAdd);
			deleteS1List.add(flowAdd);
		}
		deleteMap.put(s1, deleteS1List);
	}

	public void installOnS2(String ipv4AddressWithMask, int priority, List<Subscription> dstList, List<OFFlowAdd> deleteS2List, Map<IOFSwitch, List<OFFlowAdd>> deleteMap){
		logger.info("install flow entries on s2");
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);
		OFOxms oxms = myFactory.oxms();
		IOFSwitch s2 = switchService.getSwitch(DatapathId.of(2));
			// set match field
			Match match = myFactory.buildMatch()
					.setExact(MatchField.ETH_TYPE, EthType.IPv4)
					.setMasked(MatchField.IPV4_DST, IPv4AddressWithMask.of(ipv4AddressWithMask))
					.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
					.build();
			//set actions
			ArrayList<OFAction> actionList = new ArrayList<OFAction>();
			// iterate the subscribers who subscribe specific values
			for(Subscription sub: dstList){
				IPv4Address ipv4Dst = IPv4Address.of(sub.getIpv4Address());
				TransportPort udpDst = TransportPort.of(sub.getUdpPort());
				// Use OXM to modify network layer dest field
				OFActionSetField setNwIpDst = myFactory.actions().buildSetField()
						.setField(oxms.buildIpv4Dst().setValue(ipv4Dst).build())
						.build();
				// reset upd dst port
				OFActionSetField setNwUdpDst = myFactory.actions().buildSetField()
						.setField(oxms.buildUdpDst().setValue(udpDst).build())
						.build();
				// set output
				OFActionOutput output = myFactory.actions().buildOutput()
						.setMaxLen(0xFFffFFff)
						.setPort(ipToSwPortMap.get(ipv4Dst))
						.build();
				actionList.add(setNwIpDst);
				actionList.add(setNwUdpDst);
				actionList.add(output);
			}
			//add a flow entry
			OFFlowAdd flowAdd = myFactory.buildFlowAdd()
					.setPriority(priority)
					.setMatch(match)
					.setActions(actionList)
					.build();
			s2.write(flowAdd);
			deleteS2List.add(flowAdd);
			deleteMap.put(s2, deleteS2List);
	}

	public void installOnS3(List<OFFlowAdd> deleteS3List, Map<IOFSwitch, List<OFFlowAdd>> deleteMap){
		logger.info("install flow entries on s3");
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_14);
		IOFSwitch s3 = switchService.getSwitch(DatapathId.of(3));
		// set match field
		Match match = myFactory.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setMasked(MatchField.IPV4_DST, IPv4AddressWithMask.of("230.0.0.0/8"))
				.build();
		// add actions
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		// set output
		OFActionOutput output = myFactory.actions().buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(2))
				.build();
		actionList.add(output);
		//add a flow entry
		OFFlowAdd flowAdd = myFactory.buildFlowAdd()
				.setMatch(match)
				.setActions(actionList)
				.build();
		s3.write(flowAdd);
		deleteS3List.add(flowAdd);
		deleteMap.put(s3, deleteS3List);
	}

	public void setIpTable(){
		logger.info("initial ip to switchport table");
		ipToSwPortMap = new HashMap<>();
		ipToSwPortMap.put(IPv4Address.of("10.1.1.1"),OFPort.of(3));
		ipToSwPortMap.put(IPv4Address.of("10.1.1.2"),OFPort.of(2));
		ipToSwPortMap.put(IPv4Address.of("10.1.1.3"),OFPort.of(4));
		ipToSwPortMap.put(IPv4Address.of("10.1.1.4"),OFPort.of(5));
	}

	@Override
	public String deleteSubscription(String name) {
		logger.info("Deleting subscription " + name); // you may change logging to your liking
		String status;

		// DONE Implement!
		if (subMap.containsKey(name)) {
			status = "Successfully deleted subscription " + name;
			deleteFlow(name);
//			subMap.remove(name);		
		} else {
			status = "Error! Subscription " + name + " does not exist";
		}
		return "{\"status\":\"" + status + "\"}";
	}

	// DONE: complete method
	private void deleteFlow(String name) {
		Subscription sub = subMap.get(name);
		subMap.remove(name);
        // classify the subscriptions according to the type and comparator
        Map<IOFSwitch, List<OFFlowAdd>> delMap = (sub.isGreater()) ? greaterDelMapArr[sub.getType()] : lessDelMapArr[sub.getType()];
        List<Subscription> installList = (sub.isGreater()) ? greaterListArr[sub.getType()] : lessListArr[sub.getType()];
        logger.info("delete flow for "+name+" isGreater: "+sub.isGreater()+" value: " + sub.getrVal()+" type: " + sub.getType());
        installList.remove(sub);
        // uninstall all existing flows
        deleteFlow(delMap);
        // reinstall new flows excluding deleted subscription
        if(!installList.isEmpty()) {
            if (sub.isGreater()) {
                installGreater(installList, delMap);
            } else {
                installLessEqual(installList, delMap);
            }	
        }	
	}

}
