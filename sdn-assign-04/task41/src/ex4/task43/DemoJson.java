package ex4.task43;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;


import java.io.File;
import java.io.IOException;

public class DemoJson {

    public static void main(String[] args) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        File file = new File("test.json");
        JsonGenerator jGen = jsonFactory.createGenerator(file, JsonEncoding.UTF8);
        jGen.writeStartObject();
        jGen.writeStringField("message", "Hello world!");
        jGen.writeEndObject();
        jGen.close();

        // http://openjdk.java.net/groups/net/httpclient/recipes.html
    }
}
