package ex4.task41;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private static final class Commands {
        public static final String LISTEN = "l";
        public static final String POST = "p";
        public static final String GET = "g";
        public static final String DELETE = "d";
    }
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

    public static String post(String name, String udp, String type, String rval, boolean isFiltered, boolean isGreater) throws IOException {
        String json = "{\"udp_port\":\""  + udp + "\",\"type\":\"" + type + "\",\"filter_enable\":\"" + isFiltered + "\",\"reference_value\":\"" + rval  + "\",\"is_greater\":\"" + isGreater + "\"}";
//      String command = "curl -X POST http://10.10.10.10:8080/subscriptions/" + name + "/json -d " + json;
		String command = "curl -X POST -d " + json + " http://10.10.10.10:8080/subscriptions/" + name + "/json";
        System.out.println("Executing... " + command);
        // curl -X POST -d '{"udp_port":"50001", "type":"0", "filter_enable":"true", "reference_value":"30", "is_greater":"false"}' http://10.10.10.10:8080/subscriptions/sub1/json
        Process process = Runtime.getRuntime().exec(command);
        consumeInputStream(process.getInputStream());
        return command;
    }

    public static String delete(String name) throws IOException {
        String command = "curl -X DELETE http://10.10.10.10:8080/subscriptions/" + name + "/json";
        System.out.println("Executing... " + command);
        Process process = Runtime.getRuntime().exec(command);
        consumeInputStream(process.getInputStream());
        return command;
    }

    public static String get() throws IOException {
        String command = "curl -X GET http://10.10.10.10:8080/subscriptions/json";
        System.out.println("Executing... " + command);
        Process process = Runtime.getRuntime().exec(command);
        consumeInputStream(process.getInputStream());
        return command;
    }

    public static String inputStreamToString(InputStream inputStream) throws IOException {
        final int bufferSize = 8 * 1024;
        byte[] buffer = new byte[bufferSize];
        final StringBuilder builder = new StringBuilder();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, bufferSize);
            while (bufferedInputStream.read(buffer) != -1) {
                builder.append(new String(buffer));
            }
        System.out.println(builder.toString());
        return builder.toString();
    }

    public static void consumeInputStream(InputStream inputStream) throws IOException {
        inputStreamToString(inputStream);
    }

    public static void main(String[] args) throws IOException {
        String command = args[0];
        switch (command) {
            case Commands.DELETE:
                // Example: $ java Subscriber d sub1
                delete(args[1]);
                break;
            case Commands.LISTEN:
                // Example: $ java Subscriber l 50001 0 30 filter gt
                Subscriber subscriber = new Subscriber(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4].equals("filter") , args[5].equals("gt"));
                subscriber.listen();
                break;
            case Commands.GET:
                // Example: $ java Subscriber g
                get();
                break;
            case Commands.POST:
                // Example: $ java Subscriber p sub1 50001 0 30 filter gt
                post(args[1], args[2], args[3], args[4], args[5].equals("filter"), args[6].equals("gt"));
                break;
            default:
                System.out.println("Not a valid command");
        }
    }
}
