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

public class DecimalNumberValidator implements Validator {

    private long min;
    private long max;

    public void validates(String field, Object value, Errors errors) {
        if (value == null || value instanceof Integer || value instanceof Long) {
            return;
        }
        if (value instanceof String) {
            try {
                Long number = Long.valueOf((String) value);
                if (number < min) {
                    errors.add(field, m("errors.decimal.lower", field, min));
                }
                if (number > max) {
                    errors.add(field, m("errors.decimal.higher", field, max));
                }
                return;
            } catch (IllegalArgumentException e) {
            }
        }
        errors.add(field, m("errors.decimal.nan", field));
    }

    public DecimalNumberValidator min(long min) {
        this.min = min;
        return this;
    }

    public DecimalNumberValidator max(long max) {
        this.max = max;
        return this;
    }

}
