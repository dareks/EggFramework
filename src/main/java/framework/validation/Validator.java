package framework.validation;


public interface Validator {

	void validates(String field, Object value, Errors errors);

}
