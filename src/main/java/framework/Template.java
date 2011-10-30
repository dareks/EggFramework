/*
 *   Copyright (C) 2011 Jacek Olszak
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package framework;

import static framework.GlobalHelpers.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

public class Template {

    private static Map<String, Long> filemodificationDates = new Hashtable<String, Long>();

    private static TemplateEngine templateEngine;
    private static String fileExtension;
    private static boolean productionMode;

    static {
        productionMode = Config.isInProductionMode();
        new File("target/generated").mkdirs();
        templateEngine = new GroovyTemplateEngine();
        fileExtension = templateEngine.getFileExtension();
    }

    public static boolean exists(String template) {
        return Template.class.getResource(template + ".html") != null;
    }

    public static void render(String template, Map<String, Object> model, Writer writer) throws Exception {
        if (!template.startsWith("/")) {
            template = req().getController() + "/" + template;
        }
        if (template.contains("/")) {
            new File("target/generated/" + template.substring(0, template.lastIndexOf("/"))).mkdirs();
        }
        String groovySourceFile = generateSourceFile(template);
        long started = System.nanoTime();
        templateEngine.run(template, groovySourceFile, model);
        Loggers.BENCHMARK.info("Template {} rendering time is {} us", template, (System.nanoTime() - started) / 1000);
    }

    private static String generateSourceFile(String template) throws IOException, FileNotFoundException, URISyntaxException {
        String groovySourceFile = "target/generated/" + template + "." + fileExtension;
        if (!template.startsWith("/")) { // use indexOf instead
            template = "/" + template;
        }
        URL url = Template.class.getResource(template + ".html");
        if (url == null) {
            throw new FileNotFoundException("Template " + template + ".html does not exist");
        }

        final String name = url.getFile();
        Long date = filemodificationDates.get(name);
        // TODO Add checking if imports.groovy was modified - then flush all cache
        File file = new File(url.toURI());
        long lastModified = !productionMode ? file.lastModified() : -1; // omit lastModified() method
                                                                        // call in
        // production
        // mode (performance optimization)
        if (date == null || (!productionMode && date < lastModified)) {
            File generatedSourceFile = new File(groovySourceFile);
            FileReader reader = new FileReader(file);
            templateEngine.generate(template, reader, generatedSourceFile);
            filemodificationDates.put(name, lastModified);
        }
        return groovySourceFile;
    }

}
