package controllers;

/**
 * Basic example of Flash/Flex and Egg Framework interoperability. <br />
 * AMF HTTP is used as a transfer protocol (binary format message). <br />
 * See src/main/resources/amf/client.fla for sample client code.
 */
public class AmfController {

    /**
     * Sample code of Flash client:<br />
     * <code>
     *      import flash.net.NetConnection;
     *      var conn:NetConnection = new NetConnection();
     *      conn.connect("http://localhost:8080/amfGateway");       // gateway url is always the same
     *      conn.call("/amf/hello", new Responder(helloResult));    // helloResult function will handle the response
     * </code>
     */
    public String hello() {
        return "Hello from server :)"; // will return the string to the Flash
                                       // client
    }

    /**
     * Sample code of Flash client:<br />
     * <code>
     *      import flash.net.NetConnection;
     *      var conn = new NetConnection();
     *      conn.connect("http://localhost:8080/amfGateway");                                
     *      conn.call("/amf/toLowerCase", new Responder(helloResult), "CAPITAL LETTERS");    // string parameter passed as a last argument
     * </code>
     */
    public String toLowerCase(String string) {
        return string.toLowerCase(); // will lowercase the passed string
    }
}
