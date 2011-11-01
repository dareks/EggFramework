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
import java.io.InputStreamReader;
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
    private static File generatedDir;

    static {
        productionMode = Config.isInProductionMode();
        generatedDir = productionMode ? new File(System.getProperty("java.io.tmpdir") + File.separatorChar + "generated" + File.separatorChar +  Config.get("app.url").replace(File.separatorChar, '_')) : new File("target/generated");
        generatedDir.mkdirs();
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
        	new File(generatedDir, template.substring(0, template.lastIndexOf("/"))).mkdirs();
        }
        String sourceFile = generateSourceFile(template);
        long started = System.nanoTime();
        templateEngine.run(template, sourceFile, model);
        Loggers.BENCHMARK.info("Template {} rendering time is {} us", template, (System.nanoTime() - started) / 1000);
    }

    private static String generateSourceFile(String template) throws IOException, FileNotFoundException, URISyntaxException {
        File generatedSourceFile = new File(generatedDir, template + "." + fileExtension);
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
        long lastModified = -1;
        if (!productionMode) {
        	try {
        		lastModified = new File(url.toURI()).lastModified();
        	} catch(URISyntaxException e) {
        		lastModified = new File(url.getPath()).lastModified();
        	}
        }
        if (date == null || (!productionMode && date < lastModified)) {
        	InputStreamReader reader = new InputStreamReader(url.openStream()); // TODO Close stream/reader
            templateEngine.generate(template, reader, generatedSourceFile);
            filemodificationDates.put(name, lastModified);
        }
        return generatedSourceFile.getPath();
    }

}
