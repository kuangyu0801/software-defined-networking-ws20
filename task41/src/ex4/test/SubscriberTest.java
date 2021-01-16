package ex4.test;

import ex4.task41.Subscriber;
import org.junit.jupiter.api.Test;

import java.net.SocketException;

public class SubscriberTest {

    @Test
    private void dummyTest() throws SocketException {
        System.out.println("I'm dummy test");
        Subscriber subscriber = new Subscriber(5001, 0, 10, true, true);
    }
}
