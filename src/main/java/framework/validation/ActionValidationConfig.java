package framework.validation;

import static framework.GlobalHelpers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

import framework.Params;

public class ActionValidationConfig {

	private static final Map<String, ActionValidationConfig> config = Maps
			.newHashMap();

	private Map<String, List<Validator>> validators = Maps.newHashMap();

	String inputController;
	String inputAction;

	public static ActionValidationConfig get(String path) {
		synchronized (ActionValidationConfig.class) {
			if (!config.containsKey(path)) {
				config.put(path, new ActionValidationConfig());
			}
		}
		return config.get(path);
	}

	// TODO SKOPIOWANTY KOD Z OBJECTVALIDATIONCONFIG!
	public void validate(Params params, Errors errors) {
		Map<String, Object> mapToValidate = Maps.newHashMap();
		Map<String, String[]> map = params().getMap();
		for (Entry<String, String[]> entry : map.entrySet()) {
			Object value = (entry.getValue() != null && entry.getValue().length == 1) ? entry.getValue()[0] : entry.getValue();
			mapToValidate.put(entry.getKey(), value);
		}
		// TODO Add nested validation
		Set<Entry<String, List<Validator>>> entrySet = validators.entrySet();
		for (Entry<String, List<Validator>> entry : entrySet) {
			List<Validator> fieldValidators = entry.getValue();
			for (Validator validator : fieldValidators) {
				String key = entry.getKey();
				Object fieldValue = key != null ? mapToValidate.get(key) : mapToValidate;
				validator.validates(key, fieldValue, errors); 
			}
		}
	}

	public synchronized ActionValidationConfig add(String name,
			Validator... validators) {
		List<Validator> list = this.validators.get(name);
		if (list == null) {
			this.validators.put(name, new ArrayList<Validator>());
		}
		for (Validator v : validators) {
			this.validators.get(name).add(v);
		}
		return this;
	}
	
	public synchronized ActionValidationConfig setValidators(Map<String, List<Validator>> validators) {
		this.validators = validators;
		return this;
	}
	
	public void input(String action) {
		this.inputController = null;
		this.inputAction = action;
	}

	public void input(String controller, String action) {
		this.inputController = controller;
		this.inputAction = action;
	}

	public String getInputPath() {
		String controller = inputController != null ? inputController : req().getController();
		String action = inputAction != null ? inputAction : "index";
		return f("/%s/%s", controller, action);
	}
}
