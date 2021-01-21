package net.sdnlab.ex4.task43;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonEncoding;

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
	
	private Map<String, Subscription> subMap = new HashMap<>();

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
			createFlow(name, sub);
			installFlow(name, sub);
		}
		 
		return "{\"status\":\"" + status + "\"}";
	}

	// TODO: complete method
	private void installFlow(String name, Subscription sub) {
		
	}
	
	// TODO: complete method
	private void createFlow(String name, Subscription sub) {
		
	}

	@Override
	public String deleteSubscription(String name) {
		logger.info("Deleting subscription " + name); // you may change logging to your liking
		String status;

		// DONE Implement!
		if (subMap.containsKey(name)) {
			status = "Successfully deleted subscription " + name;
			subMap.remove(name);
			deleteFlow(name);
		} else {
			status = "Error! Subscription " + name + " does not exist";
		}
		return "{\"status\":\"" + status + "\"}";
	}

	// TODO: complete method
	private void deleteFlow(String name) {
		
		
	}

}
