package framework;

/**
 * Run this class in order to run a web server with your application. To enable
 * automatic reloading use JRebel ( {@link http
 * ://www.zeroturnaround.com/jrebel/} )
 * 
 * @author Jacek Olszak
 */
public class Server {

    public static void main(String[] args) throws InterruptedException {
        ForkedJettyServer server = new ForkedJettyServer();
        server.start();
    }
}
