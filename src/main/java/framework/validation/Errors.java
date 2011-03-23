package framework.validation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Errors {

	private Map<String, List<String>> messages = Maps.newHashMap();

	public boolean hasErrors() {
		return messages.size() > 0;
	}

	public synchronized void add(String msg) {
		add(null, msg);
	}

	public synchronized void add(String field, String msg) {
		List<String> list = messages.get(field);
		if (list == null) {
			list = Lists.newArrayList();
			messages.put(field, list);
		}
		list.add(msg);
	}

	public synchronized void add(String field, List<String> msg) {
		List<String> list = messages.get(field);
		if (list == null) {
			list = Lists.newArrayList();
			messages.put(field, list);
		}
		list.addAll(msg);
	}

	public Map<String, List<String>> getMessages() {
		return messages;
	}

	public List<String> getMessages(String field) {
		return messages.get(field);
	}

	@Override
	public String toString() {
		return "Errors [messages=" + messages + "]";
	}
	
}
