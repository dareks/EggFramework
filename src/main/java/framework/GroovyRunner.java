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

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;

import java.io.File;
import java.io.IOException;

public interface GroovyRunner {

    void run(String scriptName, Binding binding) throws Exception;

    /**
     * Supports auto-reloading
     */
    public static final class GroovyScriptEngineRunner implements GroovyRunner {

        private GroovyScriptEngine gse;

        public GroovyScriptEngineRunner() throws IOException {
            gse = new GroovyScriptEngine(new String[] { "" });
        }

        public void run(String scriptName, Binding binding) throws Exception {
            gse.run(scriptName, binding);
        }

    }

    /**
     * Fast - for production use
     */
    public static final class GroovyClassLoaderRunner implements GroovyRunner {

        private GroovyClassLoader gcl;

        public GroovyClassLoaderRunner() {
            gcl = new GroovyClassLoader();
        }

        public void run(String scriptName, Binding binding) throws Exception {
            Class<?> clazz = gcl.parseClass(new GroovyCodeSource(new File(scriptName)), true);
            Script script = (Script) clazz.newInstance();
            script.setBinding(binding);
            script.invokeMethod("run", new Object[0]);
        }
    }
}
