package framework;

import java.util.Map;

public interface Page {

    void render(Map<String, Object> model) throws Exception;

}
