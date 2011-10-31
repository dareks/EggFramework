package controllers;

import static framework.GlobalHelpers.*;
import framework.Response;
import framework.validation.Errors;
import framework.validation.Validator;

/**
 * Please note that current forms support is in experimental phase and is subject to change.
 */
public class FormsController {

    static {
        // validation rules specified in CreateUserForm class will be used for action createUser
        validateParams("createUser", CreateUserForm.class).input("validatedForm"); // if validation error occurs go back
                                                                                   // to validatedForm action
    }

    /**
     * Action which prepares a form attribute
     */
    public void validatedForm() {
        CreateUserForm form = paramsAsBean(CreateUserForm.class); // map incoming parameters to CreateUserForm
        attr("form", form);
    }

    /**
     * Mthod executed after submitting the form this method will be executed only if the validation rules are met. <br />
     * Otherwise it will create "errors" attribute and return to the validatedForm action.
     */
    public Response createUser() {
        CreateUserForm form = paramsAsBean(CreateUserForm.class);
        return renderText(f("User with name %s created", form.name));
    }

    /**
     * Class responsible for holding incoming parameters
     */
    public static class CreateUserForm {

        static {
            required("name", "street"); // name and street parameters are required which means that they shouldn't be
                                        // null or empty
            registerValidator("postalCode", new PostalCodeValidator()); // for postalCode parameter use custom validator
            email("email");
        }

        public String name = "";
        public String street = "";
        public String postalCode = "";
        public String city = "";
        public String country = "";
        public String email = "";

    }

    /**
     * Example of custom validator validating postal code format (xx-xxx)
     */
    public static class PostalCodeValidator implements Validator {

        public void validates(String field, Object value, Errors errors) {
            if (value != null && !String.valueOf(value).trim().isEmpty()) { // validate only if parameter is given and
                                                                            // is not empty
                String val = value.toString();
                if (!val.matches("\\d\\d-\\d\\d\\d")) {
                    errors.add(field, m("errors.postalCode"));
                }
            }
        }
    }

}
