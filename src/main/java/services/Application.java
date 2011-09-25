package services;

import static framework.GlobalHelpers.*;
import framework.Rule;

public class Application {

    public void start() throws Exception {
        System.out.println("Starting...");

        routing().addRule(new Rule("/index").rewrite("/sample/index"));
        routing().addRule(new Rule("/$controller/$action"));
    }

    public void stop() {
        System.out.println("Stopping...");
    }

}
