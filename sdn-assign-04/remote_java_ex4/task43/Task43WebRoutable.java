package net.sdnlab.ex4.task43;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class Task43WebRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		router.attach("/json", ListSubscriptionsResource.class);
		router.attach("/{name}/json", SubscriptionResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/subscriptions";
	}

}
