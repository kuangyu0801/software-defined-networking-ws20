package ex4.test;

import ex4.task41.EchoServer;
import ex4.task41.Subscriber;
import org.junit.Test;
import java.io.IOException;

public class SubscriberTest {

    @Test
    public void dummyTest() throws IOException {
        System.out.println("I'm dummy test");
        int udpPort = 5001;
        Subscriber subscriber = new Subscriber(udpPort, 0, 10, true, true);
        EchoServer echoServer = new EchoServer(udpPort + 1);
        echoServer.start();
        subscriber.test();

    }

}
