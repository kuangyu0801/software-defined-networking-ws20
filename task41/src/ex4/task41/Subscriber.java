package ex4.task41;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Logger;

public class Subscriber {
    private static final Logger logger = Logger.getLogger(Subscriber.class.getSimpleName());
    private static final String TAG = "[Subscriber]";
    private DatagramSocket datagramSocket;
    private int udpPort;
    private int type; // measurement type: 0: energy, 1: power
    private int rVal; // reference value
    private boolean isFilterAll;
    private boolean isGreater; // comparator: true: greater than, false: less and equal

    /**
     *
     * @param udpPort
     * @param type
     * @param rVal
     * @param isFilterAll
     * @param isGreater
     * @throws SocketException
     */
    public Subscriber(int udpPort, int type, int rVal, boolean isFilterAll, boolean isGreater) throws SocketException {
        this.udpPort = udpPort;
        this.type = type;
        this.rVal = rVal;
        this.isFilterAll = isFilterAll;
        this.isGreater = isGreater;
        datagramSocket = new DatagramSocket(udpPort);
        logger.info("[Init Subscriber] " + "UDP port: " + udpPort + ", Type: " + ((type == 0) ? "Energy" : "Power") +
                ", reference Value: " + rVal +  "Filter All: " + isFilterAll + "Comparator: " + ((isGreater) ? ">": "<=" ));
        System.out.println("Subscriber Initiated");
    }

    public void listen() throws IOException {
        System.out.println("Listening");
        while (true) {
            receive();
        }
    }

    private void receive() throws IOException {
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        datagramSocket.receive(receivePacket);
        String receiveData = new String(receivePacket.getData());
        System.out.println("[Subscriber][R]" + receiveData.trim());
    }

    /**
     *  this method used for testing
     * @throws IOException
     */
    public void test() throws IOException {
        String msg = "Hello World!";
        byte[] buf = msg.getBytes();
        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort + 1);
        datagramSocket.send(packet);
        System.out.println("[Subscriber][S]" + msg);
        receive();
    }

    public static void main(String[] args) throws IOException {
        Subscriber subscriber = new Subscriber(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3].equals("all") , args[4].equals("gt"));
        subscriber.listen();
    }
}
