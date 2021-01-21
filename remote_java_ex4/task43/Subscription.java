/**
 * 
 */
package net.sdnlab.ex4.task43;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

/**
 * @author student
 * Class for manage subscription
 */
public class Subscription {
	
	protected static Logger logger = LoggerFactory.getLogger(Subscription.class);
    private int udpPort;
    private int type; // measurement type: 0: energy, 1: power
    private int rVal; // reference value
    private boolean isFiltered;
    private boolean isGreater; // comparator: true: greater than, false: less and equal
    
    public Subscription(int udpPort, int type, int rVal, boolean isFiltered, boolean isGreater)  {
        this.setUdpPort(udpPort);
        this.setType(type);
        this.setrVal(rVal);
        this.setFiltered(isFiltered);
        this.setGreater(isGreater);
    }

	public int getUdpPort() {
		return udpPort;
	}

	public void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getrVal() {
		return rVal;
	}

	public void setrVal(int rVal) {
		this.rVal = rVal;
	}

	public boolean isFiltered() {
		return isFiltered;
	}

	public void setFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}

	public boolean isGreater() {
		return isGreater;
	}

	public void setGreater(boolean isGreater) {
		this.isGreater = isGreater;
	}

	/**
	 *
	 * @param fmJson
	 * @return
	 * @throws IOException
	 * reference: https://github.com/floodlight/floodlight/blob/d737cb05656a6038f4e2277ffb4503d45b7b29cb/src/main/java/net/floodlightcontroller/staticentry/StaticEntries.java#L109 
	 */
	public static Subscription jsonToSubscription(String fmJson) throws IOException {

		MappingJsonFactory f = new MappingJsonFactory();
		JsonParser jp = null;

		try {
			jp = f.createParser(fmJson);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Integer udpPort = null;
		Integer type = null; // measurement type: 0: energy, 1: power
		Integer rVal = null; // reference value
		Boolean isFiltered = null; // true: is filter enabled
		Boolean isGreater = null; // comparator: true: greater than, false: less and equal

		jp.nextToken();

		if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected START_OBJECT");
		}

		while (jp.nextToken() != JsonToken.END_OBJECT) {
			if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
				throw new IOException("Expected FIELD_NAME");
			}

			String n = jp.getCurrentName().toLowerCase().trim();
			
			jp.nextToken();
			
			if (n.equals(Task43.Columns.COLUMN_FILTER_ENALBE)) {
				isFiltered = Boolean.parseBoolean(jp.getText());
			} else if (n.equals(Task43.Columns.COLUMN_UDP_PORT)) {
				udpPort = Integer.parseInt(jp.getText());
			} else if (n.equals(Task43.Columns.COLUMN_TYPE)) {
				type = Integer.parseInt(jp.getText());
			} else if (n.equals(Task43.Columns.COLUMN_REFERENCE_VALUE)) {
				rVal = Integer.parseInt(jp.getText());
			} else if (n.equals(Task43.Columns.COLUMN_IS_GREATER)) {
				isGreater = Boolean.parseBoolean(jp.getText());
			}
		}
		logger.info("Create new Subscription " + "UDP port: " + udpPort + ", Type: " + ((type == 0) ? "Energy" : "Power") +
                ", reference Value: " + rVal +  ", Filter: " + ((isFiltered) ? ("Enabled") : "Disabled") + ", Comparator: " + ((isGreater) ? ">": "<=" ));
		Subscription sub = new Subscription(udpPort, type, rVal, isFiltered, isGreater);
		return sub;
	}
}
