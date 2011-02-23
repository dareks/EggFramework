package framework.validation;

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
					errors.add(field, field + " is lower than " + min);
				}
				if (number > max) {
					errors.add(field, field + " is higher than " + max);
				}
				return;
			} catch (IllegalArgumentException e) {
			}
		}
		errors.add(field, field + " is not an integer number");
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
