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
package framework.validation;

import static framework.GlobalHelpers.*;

import java.util.regex.Pattern;

public class RegexpValidator implements Validator {

    private Pattern pattern;
    private String messageKey;

    public RegexpValidator(String pattern, String messageKey) {
        this.pattern = Pattern.compile(pattern);
        this.messageKey = messageKey;
    }

    public RegexpValidator(String pattern) {
        this(pattern, null);
    }

    public void validates(String field, Object value, Errors errors) {
        if (value != null && !String.valueOf(value).isEmpty()) {
            if (!pattern.matcher(String.valueOf(value)).matches()) {
                if (messageKey != null) {
                    errors.add(field, m(messageKey));
                } else {
                    errors.add(field, m("errors.regexp", pattern.pattern()));
                }
            }
        }
    }

}
