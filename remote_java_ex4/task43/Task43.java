package net.sdnlab.ex4.task43;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;

public class Task43 implements IFloodlightModule, ITask43Service {

	protected IFloodlightProviderService floodlightProvider;
	protected IRestApiService restApiService;

	// TODO: add any other required services

	protected static Logger logger;
	
	public static class Columns {
		public static final String COLUMN_UDP_PORT = "udp_port";
		public static final String COLUMN_TYPE = "type";
		public static final String COLUMN_REFERENCE_VALUE = "reference_value";
		public static final String COLUMN_FILTER_ENALBE = "filter_enable";
		public static final String COLUMN_IS_GREATER = "is_greater";
	}

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

		return deps;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);

		// TODO: initialize any other required services

		logger = LoggerFactory.getLogger(Task43.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		restApiService.addRestletRoutable(new Task43WebRoutable());
	}

	@Override
	public String listSubscriptions() {
		logger.info("Listing all subscriptions"); // you may change logging to your liking
		String jsonSubscriptions = "{}";

		// TODO Implement!
		System.out.println("receive a GET Request");

		return jsonSubscriptions;
	}

	@Override
	public String addSubscription(String name /* TODO: add arguments */, Subscription sub) {
		logger.info("Adding subscription " + name); // you may change logging to your liking
		String status;

		// TODO Implement!
		System.out.println("Post Request from: "+name);

		status = "Successfully added new subscription " + name;
		// status = "Error! Subscription " + name + " already exists";
		return "{\"status\":\"" + status + "\"}";
	}

	@Override
	public String deleteSubscription(String name) {
		logger.info("Deleting subscription " + name); // you may change logging to your liking
		String status;

		// TODO Implement!

		status = "Successfully deleted subscription " + name;
		// status = "Error! Subscription " + name + " does not exist";
		return "{\"status\":\"" + status + "\"}";
	}

}
