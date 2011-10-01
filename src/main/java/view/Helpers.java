package view;

import static framework.GlobalHelpers.*;
import groovy.lang.Closure;

import java.io.IOException;
import java.io.Writer;

public class Helpers {

    /**
     * Every helper method need to be a pubilc static
     */
    public static boolean isAuthenticated() {
        Boolean authenticated = session().get("authenticated");
        return authenticated != null && authenticated;
    }

    public static void bold(Closure closure) throws IOException {
        Writer out = out();
        out.append("<b>");
        call(closure);
        out.append("</b");
    }
}
