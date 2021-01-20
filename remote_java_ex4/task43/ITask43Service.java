package net.sdnlab.ex4.task43;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface ITask43Service extends IFloodlightService {
	public String listSubscriptions();
	public String addSubscription(String name /* DONE: add arguments */, Subscription sub);
	public String deleteSubscription(String name);
}
