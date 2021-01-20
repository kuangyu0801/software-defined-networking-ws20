/**
 * 
 */
package net.sdnlab.ex4.task43;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author student
 * 
 */
@JsonSerialize(using=SubscriptionMapSerializer.class) 
public class SubscriptionMap {
	/*
	 * Contains the following mapping:
	 * Map<Subscription-Name, Subscription>
	 */
	private Map<String, Subscription> theMap;
	
	public SubscriptionMap (Map<String, Subscription> theMap) {
		this.theMap = theMap;
	}
	
	public Map<String, Subscription> getMap() {
		return theMap;
	}
}
