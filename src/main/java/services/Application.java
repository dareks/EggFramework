package services;

import static framework.GlobalHelpers.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    public void start() throws Exception {
        logger.info("Starting...");

        match("/index").rewrite("/sample/index"); // main page is /sample/index action
    }

    public void stop() {
        logger.info("Stopping...");
    }

}
