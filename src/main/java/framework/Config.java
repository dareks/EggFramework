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
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

public class Config {

    private static final String FILENAME_PREFIX = "WEB-INF/config";
    private static String filename;
    private static Properties properties;
    private static volatile long lastChecked;
    private static volatile long lastModified;

    private static volatile boolean inProductionMode;

    public static final String MODE = "mode";
    public static final String PRODUCTION_MODE = "production";
    public static final String DEVELOPMENT_MODE = "development";

    static {
        String mode = System.getProperty(MODE);
        if (mode == null) {
            mode = PRODUCTION_MODE;
        }
        filename = FILENAME_PREFIX + "_" + mode + ".properties";
        load();
        inProductionMode = PRODUCTION_MODE.equalsIgnoreCase(mode);
    }

    private static void checkModifications() {
        // check for modification after 1 second
        if (System.currentTimeMillis() - lastChecked > 1000) { // TODO interval should be customizable
            lastChecked = System.currentTimeMillis();
            synchronized (Config.class) {
                if (getFile().lastModified() != lastModified) {
                    load();
                }
            }
        }
    }

    private static File getFile() {
        ServletContext ctx = FrameworkServlet.SERVLET_CONTEXT;
        if (ctx != null) {
            return new File(ctx.getRealPath(filename));
        } else {
            return new File("src/main/webapp/" + filename);
        }
    }

    private static void load() {
        properties = new Properties();
        try {
            File file = getFile();
            lastModified = file.lastModified();
            FileReader fileReader = new FileReader(file);
            properties.load(fileReader);
        } catch (FileNotFoundException e) {
            Loggers.CONFIG.error(e.getMessage(), e);
        } catch (IOException e) {
            Loggers.CONFIG.error(e.getMessage(), e);
        }
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {
        checkModifications();
        Object value = properties.get(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value != null ? processPlaceholders(String.valueOf(value)) : defaultValue;
    }

    private static String processPlaceholders(String str) {
        // TODO IMPLEMENT
        return str;
    }

    public static boolean isTrue(String key) {
        String bool = get(key, "false");
        return bool.trim().equalsIgnoreCase("true");
    }

    public static boolean isTrue(String key, boolean defaultValue) {
        String bool = get(key, Boolean.toString(defaultValue));
        return bool.trim().equalsIgnoreCase("true");
    }

    public static boolean isInProductionMode() {
        return inProductionMode;
    }

}
