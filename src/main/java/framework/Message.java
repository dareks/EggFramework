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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Internationalized String. Should be used instead of java.lang.String when you want it to be translated during
 * rendering of the page.<br />
 * <br />
 * 
 * TODO Add reloading of messages - see toString() method. Maybe use Groovy ConfigSlurper instead
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");

    public final String key;
    public final Serializable[] args;

    public Message(String key, Serializable... args) {
        this.key = key;
        for (int t = 0; t < args.length; t++) {
            String fieldKey = "field." + String.valueOf(args[t]);
            if (resourceBundle.containsKey(fieldKey)) {
                args[t] = resourceBundle.getString(fieldKey);
            }
        }
        this.args = args;
    }

    public String toString() {
        if (resourceBundle.containsKey(key)) {
            String msg = resourceBundle.getString(key);
            return MessageFormat.format(msg, args);
        } else {
            return key;
        }
    }

}
