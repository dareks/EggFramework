package view;

import static framework.GlobalHelpers.*;

public class Helpers {

    /**
     * Every helper method need to be a pubilc static
     */
    public static boolean isAuthenticated() {
        Boolean authenticated = session().get("authenticated");
        return authenticated != null && authenticated;
    }
}
