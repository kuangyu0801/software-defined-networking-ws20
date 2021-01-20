
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
    private boolean isFiltered;
    private boolean isGreater; // comparator: true: greater than, false: less and equal
    private int rcvMax = Integer.MIN_VALUE;
    private int rcvMin = Integer.MAX_VALUE;
    private int matchMax = Integer.MIN_VALUE;
    private int matchMin = Integer.MAX_VALUE;
    private int rcvCount = 0;
    private int matchCount = 0;
    /**
     *
     * @param udpPort
     * @param type
     * @param rVal
     * @param isFiltered
     * @param isGreater
     * @throws SocketException
     */
    public Subscriber(int udpPort, int type, int rVal, boolean isFiltered, boolean isGreater) throws SocketException {
        this.udpPort = udpPort;
        this.type = type;
        this.rVal = rVal;
        this.isFiltered = isFiltered;
        this.isGreater = isGreater;
        datagramSocket = new DatagramSocket(udpPort);
        logger.info("[INIT] " + "UDP port: " + udpPort + ", Type: " + ((type == 0) ? "Energy" : "Power") +
                ", reference Value: " + rVal +  ", Filter: " + ((isFiltered) ? ("Enabled") : "Disabled") + ", Comparator: " + ((isGreater) ? ">": "<=" ));
        System.out.println("Subscriber Initiated");
    }

    public void listen() throws IOException {
        System.out.println("Listening");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    System.out.println("Shouting down ...");
                    //some cleaning up code...
                    System.out.println("Unfiltered match: Total received count:" + rcvCount + ", max: " + rcvMax + ", min: " + rcvMin);
                    System.out.println("Filtered match: Total received count:" + matchCount +  ", max: " + matchMax + ", min: " + matchMin);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
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
        String[] infos = receiveData.split(",");

        int curValue = Integer.parseInt(infos[2]);
        int curType = Integer.parseInt(infos[3]);
        rcvMax = Math.max(rcvMax, curValue);
        rcvMin = Math.min(rcvMin, curValue);

        if (isFiltered) {
            if (curType == type && ((isGreater && (curValue) > rVal) || (!isGreater && curValue <= rVal))) {
                System.out.println(receiveData);
                matchMax = Math.max(matchMax, curValue);
                matchMin = Math.min(matchMin, curValue);
                matchCount += 1;
            }
        } else {
            System.out.println(receiveData);
        }
        rcvCount += 1;
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
