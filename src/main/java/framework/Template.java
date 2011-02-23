package framework;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class Template {

	private static GroovyScriptEngine gse;
	
	static {
		try {
			gse = new GroovyScriptEngine(new String[] { "" });
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void render(String template, Map<String, Object> model, Writer writer) throws IOException, ResourceException, ScriptException {
		File file = new File("target/classes/" + template + ".html");
		if (!file.exists()) {
			throw new IOException("Template " + template + " does not exist");
		}
		new File("target/generated").mkdirs(); 
		if (template.contains("/")) {
			new File("target/generated/" + template.substring(0, template.lastIndexOf("/"))).mkdirs();
		}
		String groovySourceFile = "target/generated/" + template + ".groovy";
		FileWriter generatedSourceWriter = new FileWriter(groovySourceFile);
		try {
			parse(file, generatedSourceWriter);
		} finally {
			generatedSourceWriter.close();
		}
		Binding binding = new Binding();
		if (model != null) {
			for (String key : model.keySet()) {
				binding.setVariable(key, model.get(key));
			}
		}
		gse.run(groovySourceFile, binding);
	}

	public static void main(String[] args) throws IOException, ResourceException, ScriptException {
		Map<String, Object> map = new HashMap<String, Object>();
		render("bank", map, new PrintWriter(System.out));
	}

	private static void parse(File file, Writer writer) throws FileNotFoundException, IOException {
		writer.append("import static framework.GlobalHelpers.*\n");
		writer.append("import static view.Helpers.*\n");
		writer.append("framework.ThreadData data = framework.FrontController.threadData.get()\n");
		FileReader reader = new FileReader(file);
		try {
			int s = 0;
			int ch = -1;
			writer.append("data.out.write \"");
			while ((ch = reader.read()) != -1) {
				if (ch == '<' && s == 0) {
					s = 1;
					writer.append("\"\n");
				} else if (ch == '%' && s == 1) {
					s = 2;
				} else if (s == 1) {
					s = 0;
					writer.append("data.out.write \"<");
					addChar(writer, s, ch);
				} else if (ch == '=' && s == 2) {
					s = 3;
					writer.append("data.out.write \"\" +");
				} else if (ch == '%' && s == 2) {
					s = 4;
				} else if (ch == '%' && s == 3) {
					s = 5;
				} else if (ch == '>' && s == 4) {
					s = 0;
					writer.append(";\ndata.out.write \"");
				} else if (ch == '>' && s == 5) {
					s = 0;
					writer.append("\ndata.out.write \"");
				} else if (s == 4) {
					s = 2;
					writer.append('%');
				} else if (s == 5) {
					s = 3;
					writer.append('%');
				} else {
					addChar(writer, s, ch);
				}
			}
			if (s == 0) {
				writer.append("\"");
			}
		} finally {
			reader.close();
		}
	}

	private static void addChar(Writer writer, int s, int ch) throws IOException {
		if (ch == '\r' || ch == '\n') {
			writer.write(" ");
		} else if (ch == '"' && s != 3) {
			writer.append("\\\"");
		} else if (ch == '$') {
			writer.append("\\$");
		} else {
			writer.write(ch);
		}
	}

}
