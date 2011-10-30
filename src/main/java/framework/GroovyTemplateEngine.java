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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import framework.GroovyRunner.GroovyClassLoaderRunner;
import framework.GroovyRunner.GroovyScriptEngineRunner;
import groovy.lang.Binding;

/**
 * Generates Groovy code
 */
public class GroovyTemplateEngine implements TemplateEngine {

    private GroovyRunner groovyRunner;
    private boolean productionMode;

    public GroovyTemplateEngine() {
        try {
            productionMode = Config.isInProductionMode();
            if (productionMode) {
                groovyRunner = new GroovyClassLoaderRunner();
            } else {
                groovyRunner = new GroovyScriptEngineRunner();
            }
        } catch (Exception e) {
            Loggers.TEMPLATE.error(e.getMessage(), e);
        }
    }

    public void run(String template, String groovySourceFile, Map<String, Object> model) throws Exception {
        Binding binding = new Binding();
        if (model != null) {
            for (String key : model.keySet()) {
                binding.setVariable(key, model.get(key));
            }
        }
        groovyRunner.run(groovySourceFile, binding);
    }

    public void generate(String name, Reader reader, File outputFile) throws FileNotFoundException, IOException {
        List<String> imports = IOUtils.readLines(Template.class.getResourceAsStream("/imports.groovy"));
        FileWriter writer = new FileWriter(outputFile);
        try {
            for (String line : imports) {
                writer.append(line).append('\n');
            }
            writer.append("framework.ThreadData data = framework.FrontController.threadData.get()\n");
            writer.append("Writer out = data.out\n");

            int s = 0;
            int ch = -1;
            writer.append("out.write '");
            while ((ch = reader.read()) != -1) {
                if (ch == '<' && s == 0) {
                    s = 1;
                    writer.append("'\n");
                } else if (ch == '%' && s == 1) {
                    s = 2;
                } else if (s == 1) {
                    s = 0;
                    writer.append("out.write '<");
                    addChar(writer, s, ch);
                } else if (ch == '=' && s == 2) {
                    s = 3;
                    writer.append("out.write '' + (");
                } else if (s == 2) {
                    s = 6;
                    addChar(writer, s, ch);
                } else if (ch == '%' && s == 6) {
                    s = 4;
                } else if (ch == '%' && s == 3) {
                    s = 5;
                } else if (ch == '>' && s == 4) {
                    s = 0;
                    writer.append("\nout.write '");
                } else if (ch == '>' && s == 5) {
                    s = 0;
                    writer.append(")\nout.write '");
                } else if (s == 4) {
                    s = 2;
                    writer.append('%');
                } else if (s == 5) {
                    s = 3;
                    writer.append('%');
                } else if (ch == '\n') {
                    if (s == 0) {
                        writer.append("\\n'\nout.write '");
                    } else {
                        writer.append(" ");
                    }
                    s = 0;
                } else if (ch == '\r') {
                    if (s != 0) {
                        writer.append(" ");
                    }
                } else {
                    addChar(writer, s, ch);
                }
            }
            if (s == 0) {
                writer.append("'");
            }
        } finally {
            reader.close();
            writer.close();
        }
    }

    private static void addChar(Writer writer, int s, int ch) throws IOException {
        if (ch == '\'' && s != 3 && s != 6) {
            writer.append("\\'");
        } else if (ch == '\\') {
            writer.append("\\\\");
        } else if (ch == '$') {
            writer.append("\\$");
        } else {
            writer.write(ch);
        }
    }

    public String getFileExtension() {
        return "groovy";
    }
}
