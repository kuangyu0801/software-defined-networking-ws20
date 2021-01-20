/**
 * 
 */
package net.sdnlab.ex4.task43;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author student
 * Function: Serialize subscription class
 * Reference: https://github.com/floodlight/floodlight/blob/master/src/main/java/net/floodlightcontroller/staticentry/web/SFPEntryMapSerializer.java
 */
public class SubscriptionMapSerializer extends JsonSerializer<SubscriptionMap>{

	@Override
	public void serialize(SubscriptionMap sm, JsonGenerator jGen, SerializerProvider serializer)
			throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true); // IMHO this just looks nicer and is easier to read if everything is quoted

		if (sm == null) {
			jGen.writeStartObject();
			jGen.writeString("No subscription have been added to the Subscrib Service.");
			jGen.writeEndObject();
			return;
		}
		
		Map<String, Subscription> theMap = sm.getMap();
		
		jGen.writeStartObject();
		if (theMap.keySet() != null) {
			for (String name: theMap.keySet()) {
				if (theMap.get(name) != null) {
					Subscription sub = theMap.get(name);
					jGen.writeArrayFieldStart(name);
					jGen.writeStartObject();
					jGen.writeNumberField("updport", sub.getUdpPort());
					jGen.writeBooleanField("filter_enable", sub.isFiltered());
					jGen.writeNumberField("type", sub.getType());
					jGen.writeNumberField("reference_value", sub.getrVal());
					jGen.writeBooleanField("is_greater", sub.isGreater());
					jGen.writeEndObject();
					jGen.writeEndArray();
				}
			}
		}
		jGen.writeEndObject();
		
	}

}
