package controllers;

import static framework.GlobalHelpers.*;

import java.util.HashMap;
import java.util.Map;

import framework.Config;
import framework.Response;

/**
 * Shows basic usage of Egg Framework. Controller is a class containing a list of action methods. Every action is
 * reponsible for handling one type of HTTP request. Egg Framework is using Convention over Configuration, which means
 * that the amount of configuration is very limited and conventions are used to configure the application automatically.
 * For instance every action defined in controller class is accessible from following url:
 * http://localhost:8080/$controllerClassNameWithoutControllerSuffix/$action
 */
public class SampleController {

    /**
     * Go to http://localhost:8080/sample/index.html to see this page. <br />
     * <br />
     * This action automatically render sample/index.html Groovy template. To change the template edit the
     * src/main/resources/sample/index.html file. To change the layout edit the src/main/resources/sample/layout.html
     * file.
     */
    public void index() {
        attr("name", "Egg Framework"); // sets an attribute named "name"
    }

    /**
     * Go to http://localhost:8080/sample/text.html to see this page. <br />
     * <br />
     * This action renders text directly without going to Groovy template.
     */
    public Response text() {
        return renderText("This is a text returned directly");
    }

    /**
     * This method is private which indicates that it is not an action and will not be executed
     */
    private Response hidden() {
        return renderText("This will never execute");
    }

    /**
     * Go to http://localhost:8080/sample/json.html to see this page. <br />
     * <br />
     * This action renders object as a JSON string.
     */
    public Response json() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "Egg Framework");
        map.put("language", "Java");
        return renderJSON(map);
    }

    /**
     * Go to http://localhost:8080/sample/partial.html to see this page. <br />
     * <br />
     * This action renders sample/index.html template without the layout (only the content itself).
     */
    public Response partial() {
        attr("name", "Egg Framework");
        return renderPartial("index");
    }

    /**
     * Go to http://localhost:8080/sample/forwardTest.html. You will be forwarded to the sample/index action
     */
    public Response forwardTest() {
        return renderAction("index"); // forward to action "index" in the same controller
    }

    /**
     * Go to http://localhost:8080/sample/redirectTest.html. You will be redirected to the sample/index action
     */
    public Response redirectTest() {
        return redirect().action("index"); // redirect to action "index" in the same controller
    }

    /**
     * Go to http://localhost:8080/sample/configTest.html to see this page. You can access config.properties from every
     * possible place (controllers, view, services etc.).
     */
    public Response configTest() {
        return renderText(f("The application is in %s mode", config(Config.MODE)));
    }
}
