package net.sdnlab.ex4.task43;

import java.io.IOException;

import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class SubscriptionResource extends ServerResource {

	@Post
	public String postSubscription(String json) {
		ITask43Service task43Service = (ITask43Service) getContext().getAttributes()
				.get(ITask43Service.class.getCanonicalName());
		String name = (String) getRequestAttributes().get("name");
		String subIP = (String) getRequest().getClientInfo().getAddress();
		System.out.println("Subscriber IP address: "+subIP);
		Subscription sub = null;
		// DONE: parse JSON
		try {
			sub = Subscription.jsonToSubscription(json,subIP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Post Request: "+json);

		return task43Service.addSubscription(name , sub/* DONE: add arguments */);
	}

	@Delete
	public String deleteSubscription() {
		ITask43Service task43Service = (ITask43Service) getContext().getAttributes()
				.get(ITask43Service.class.getCanonicalName());
		String name = (String) getRequestAttributes().get("name");
		return task43Service.deleteSubscription(name);
	}

}
