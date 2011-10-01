package controllers;

/**
 * Basic example of Flash/Flex and Egg Framework interoperability. AMF HTTP is used as a transfer protocol (binary format message). <br />
 * TODO Add sample Flash file
 */
public class AmfController {

    /**
     * Use http://localhost:8080/amfGateway as net connection url in your Flash/Flex client.<br />
     * Make a call to "amf/hello" using conn.call()
     */
    public String hello() {
        return "Hello from server :)"; // will return the string to the Flash client
    }

    /**
     * Make a call to "amf/toLowerCase" using conn.call() and pass the string parameter
     */
    public String toLowerCase(String string) {
        return string.toLowerCase(); // will lowercase the passed string
    }
}
