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
        String val = value != null ? String.valueOf(value) : "";
        if (!pattern.matcher(val).matches()) {
            if (messageKey != null) {
                errors.add(field, m(messageKey));
            } else {
                errors.add(field, m("errors.regexp", pattern.pattern()));
            }
        }
    }

}
