package framework;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Config {
	
	private static Properties properties;

	static {
		properties = new Properties();
		try {
			properties.load(new FileReader("src/main/webapp/WEB-INF/config.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String get(String key) {
		return "" + properties.get(key);
	}
	
}
