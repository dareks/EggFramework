package controllers;

import static framework.GlobalHelpers.*;
import framework.Response;

/**
 * This example shows how simple authentication can be made using before advice. For more sophisticated intecepting use
 * standard JavaServlet filters.
 */
public class BeforeController {

    /**
     * Runs before every action in this controller (or derived controllers)
     */
    public Response before() {
        Boolean session = session("authenticated"); // returns the value of
                                                    // "authenticated" session's
                                                    // attribute
        boolean authenticated = session != null && session;
        if (!authenticated) {
            session("returnUrl", req().getURL()); // sets session attribute
                                                  // "returnUrl" to the request
                                                  // URL
            return redirect().action("auth", "login"); // redirects to action in
                                                       // different controller
        } else {
            return null; // execute action methods
        }
    }

    /**
     * Go to http://localhost:8080/before/securityTest.html to see this page. <br />
     * <br />
     * Run only if user is autenticated
     */
    public Response securityTest() {
        return renderText("You are authenticated therefore you can see this text");
    }
}
