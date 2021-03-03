package net.sdnlab.ex4.task43;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class ListSubscriptionsResource extends ServerResource {

	@Get("json")
	public String getSubscriptions() {
		ITask43Service task43Service = (ITask43Service) getContext().getAttributes()
				.get(ITask43Service.class.getCanonicalName());
		return task43Service.listSubscriptions();
	}

}
