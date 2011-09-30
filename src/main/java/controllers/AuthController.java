package controllers;

import static framework.GlobalHelpers.*;
import framework.Response;

/**
 * Very simple controller for authenticating users
 */
public class AuthController {

    /**
     * Executed when user is sending the login form
     */
    public Response submitLoginForm() {
        if ("admin".equals(param("login")) && "admin".equals(param("password"))) { // param returns HTTP parameter
            session("authenticated", true); // sets session attribute authenticated to "true"
            // if returnUrl was previously saved into the session then redirect to this URL, otherwise go to main page
            String returnUrl = session("returnUrl");
            if (returnUrl != null) {
                session("returnUrl", "");
                return redirect().url(returnUrl);
            } else {
                return redirect().action("sample", "index");
            }
        } else {
            flash("error", "Wrong credentials"); // flash is a special attributes scope (persisted in session for the next request only)
            return render("login");
        }
    }

}
