/**
 * 
 */
package net.sdnlab.ex4.task43;

/**
 * @author student
 * Class for manage subscription
 */
public class Subscription {
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
}
