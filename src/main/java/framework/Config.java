package framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Config {

	private static final String FILENAME = "src/main/webapp/WEB-INF/config.properties";
	private static Properties properties;
	private static volatile long lastChecked;
	private static volatile long lastModified;

	static {
		load();
	}

	private static void checkModifications() {
		// check for modification after 1 second
		if (System.currentTimeMillis() - lastChecked > 1000) {
			lastChecked = System.currentTimeMillis();
			synchronized (Config.class) {
				if (new File(FILENAME).lastModified() != lastModified) {
					load();
				}
			}
		}
	}

	private static void load() {
		properties = new Properties();
		try {
			File file = new File(FILENAME);
			lastModified = file.lastModified();
			FileReader fileReader = new FileReader(file);
			properties.load(fileReader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String get(String key) {
		checkModifications();
		return String.valueOf(properties.get(key));
	}

	public static String get(String key, String defaultValue) {
		checkModifications();
		Object value = properties.get(key);
		return value != null ? String.valueOf(value) : defaultValue;
	}

	public static boolean isTrue(String key) {
		String bool = get(key, "false");
		return bool.trim().equalsIgnoreCase("true");
	}

	public static boolean isTrue(String key, boolean defaultValue) {
		String bool = get(key, Boolean.toString(defaultValue));
		return bool.trim().equalsIgnoreCase("true");
	}

}
