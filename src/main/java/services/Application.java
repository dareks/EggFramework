package services;

import static framework.GlobalHelpers.*;

public class Application {

    public void start() throws Exception {
        System.out.println("Starting...");

        match("/index").rewrite("/sample/index"); // main page is /sample/index action
        match("/$controller/$action"); // for /sample/index the $controller will be "sample" and $action will be "index"
    }

    public void stop() {
        System.out.println("Stopping...");
    }

}
