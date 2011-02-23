package controllers;

import static framework.GlobalHelpers.*;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.google.common.collect.ImmutableList;

import framework.GlobalHelpers;
import framework.validation.Errors;

public class SomeForm {

	String id;
	String name;
	int age;
	String password1;
	String password2;

	static {
		required("id", "age");
		decimalNumber("age").min(0).max(256);
	}
	
	public void validate2(Errors errors) {
		if (!password1.equals(password2)) {
			errors.add("Passwords not equal");
		}
	}

	public static void main(String[] args) {
		SomeForm o = new SomeForm();
//		o.id = "asd";
		o.password1 = "1";
		o.password2 = "2";
		Errors errors = validate(o);
		Set<Entry<String, List<String>>> entrySet = errors.getMessages().entrySet();
		for (Entry<String, List<String>> entry : entrySet) {
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}
		
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<SomeForm>> violations = validator.validate(o);
        for (ConstraintViolation<SomeForm> constraintViolation : violations) {
			System.out.println(constraintViolation);
		}
        
        Errors err = validateParam("id", decimalNumberValidator);
        if (err.hasErrors()) {
        	// piece of logic 
        	String id = param("id");
        } else {
        	// setErrors(err);
        }
        
        Errors err2 = validateParams(SomeForm.class); 
        if (err2.hasErrors()) {
        	SomeForm form = paramsAsBean(SomeForm.class);
        	// piece of logic 
        	String id = form.id;
        } else {
        	// setErrors(err);
        }
        
        
        // 
	}

}
