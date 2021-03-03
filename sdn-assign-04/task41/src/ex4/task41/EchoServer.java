package ex4.task41;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class EchoServer extends Thread {

        private DatagramSocket socket;
        private boolean running;
        private byte[] buf = new byte[256];

    public EchoServer(int udpPort) throws SocketException {
        socket = new DatagramSocket(udpPort);
    }

    public void run() {
        running = true;

        while (running) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("[EchoServer][R]" + received.trim());
            running = false;

            try {
                socket.send(packet);
                System.out.println("[EchoServer][S]" + received.trim());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }
}
