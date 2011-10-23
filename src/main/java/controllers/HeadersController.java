package controllers;

import static framework.GlobalHelpers.*;
import framework.Response;

public class HeadersController {

    public void before() {
        // cache every action on the browser/proxy side for 100 seconds
        header("Cache-Control", "max-age=100");
    }

    public Response cachedContent() {
        return renderText("Cached content");
    }

    public Response bigCachedContent() {
        StringBuilder builder = new StringBuilder();
        for (int t = 0; t < 1500; t++) {
            builder.append("aaaaaaaaaa");
        }
        return renderText(builder.toString());
    }

}
