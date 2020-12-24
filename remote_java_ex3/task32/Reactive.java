package net.sdnlab.ex3.task32;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
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
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.linkdiscovery.internal.LinkInfo;

public class Reactive implements IFloodlightModule, IOFMessageListener {
	// Since we are listening to OpenFlow messages we need to register with the FloodlightProvider (IFloodlightProviderService class)
	protected IFloodlightProviderService floodlightProvider;
	protected IOFSwitchService switchService;
	protected ILinkDiscoveryService linkDiscoverer;
	private Set<DatapathId> setDpids;
	private Map<DatapathId, Set<Link>> mapSwitchLinks;
	// TODO: export logger output into log file
	private static final Logger logger = Logger.getLogger(Reactive.class.getSimpleName());
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

	// TODO: STEP-1: link state detection: create directed graph for djikstra
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
}
