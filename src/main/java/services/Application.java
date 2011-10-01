package services;

import static framework.GlobalHelpers.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class);
    // Field containing reference to sampleService which can be used by controllers
    public SampleService sampleService;

    /**
     * Executed when application starts or just before first request is handled
     */
    public void start() throws Exception {
        logger.info("Starting...");

        // IoC - create context by hand or use third party libraries (if you really need these)
        sampleService = new SampleService();

        match("/index").rewrite("/sample/index"); // main page is /sample/index action
        match("/example/$action").rewrite("/sample/$action"); // all /example/* will be routed to /sample/*
    }

    public void stop() {
        logger.info("Stopping...");
    }

}
