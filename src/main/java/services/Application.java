package services;

import static framework.GlobalHelpers.*;

public class Application {

    public void start() throws Exception {
        System.out.println("Starting...");

        match("/index").rewrite("/sample/index");
        match("/$controller/$action");
    }

    public void stop() {
        System.out.println("Stopping...");
    }

}
