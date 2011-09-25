package controllers;

import static framework.GlobalHelpers.*;

public class SampleController {

    /**
     * Go to http://localhost:8080/sample/index.html to see this page
     */
    public void index() {
        attr("name", "Egg Framework");
    }

}
