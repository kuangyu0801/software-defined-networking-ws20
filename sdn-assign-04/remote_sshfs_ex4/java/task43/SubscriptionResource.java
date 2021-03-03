package net.sdnlab.ex4.task43;

import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class SubscriptionResource extends ServerResource {

	@Post
	public String postSubscription(String json) {
		ITask43Service task43Service = (ITask43Service) getContext().getAttributes()
				.get(ITask43Service.class.getCanonicalName());
		String name = (String) getRequestAttributes().get("name");

		// TODO: parse JSON

		return task43Service.addSubscription(name /* TODO: add arguments */);
	}

	@Delete
	public String deleteSubscription() {
		ITask43Service task43Service = (ITask43Service) getContext().getAttributes()
				.get(ITask43Service.class.getCanonicalName());
		String name = (String) getRequestAttributes().get("name");
		return task43Service.deleteSubscription(name);
	}

}
