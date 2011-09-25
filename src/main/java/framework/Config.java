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

public class Config {

    private static final String FILENAME = "src/main/webapp/WEB-INF/config.properties";
    private static Properties properties;
    private static volatile long lastChecked;
    private static volatile long lastModified;

    static {
        load();
    }

    private static void checkModifications() {
        // check for modification after 1 second
        if (System.currentTimeMillis() - lastChecked > 1000) {
            lastChecked = System.currentTimeMillis();
            synchronized (Config.class) {
                if (new File(FILENAME).lastModified() != lastModified) {
                    load();
                }
            }
        }
    }

    private static void load() {
        properties = new Properties();
        try {
            File file = new File(FILENAME);
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

}
