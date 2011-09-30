package controllers;

import static framework.GlobalHelpers.*;
import services.Application;
import framework.Response;

public class IocController {

    // Egg Framework autimatically injects Application instance into every controller with "app" field (autowire by name)
    // Only this one instance can be injected into controllers. You cannot inject particular services into controller. Use fields or getters in Application instance returning services references.
    private Application app;

    /**
     * Go to http://localhost:8080/ioc/runService.html to run this action.
     */
    public Response runService() {
        String result = app.sampleService.doIt(); // run service method
        return renderText(result);
    }
}
