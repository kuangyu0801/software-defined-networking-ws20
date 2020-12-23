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
	private Set<DatapathId> setDatapathId;

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
		logger.info("Get all DPID");
		setDatapathId = switchService.getAllSwitchDpids();
		// wait until all the switches has been add to network
		// TODO: remove this method to a more steady state
		while (setDatapathId.size() < 10) {
			setDatapathId = switchService.getAllSwitchDpids();
		}
		for (DatapathId dpid: setDatapathId) {
			logger.info(dpid.toString());
		}
		logger.info("Total switch number:" + setDatapathId.size());
		Map<Link, LinkInfo> links = linkDiscoverer.getLinks();
		logger.info("Get all links");
		// TODO: remove this method to a more steady state
		for (Link link : links.keySet()) {
			logger.info(link.toString());
		}

		logger.info("Start Up End");
	}

}
