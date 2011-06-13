/*
 *   Copyright (C) 2011 Jacek Olszak
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package framework;

//import groovy.lang.Binding;
//import groovy.util.GroovyScriptEngine;
import static framework.GlobalHelpers.*;
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
import java.util.Hashtable;
import java.util.Map;

public class Template {

	// TODO USE GroovyClassLoader on production instead
	private static GroovyScriptEngine gse;
	private static Map<String, Long> filemodificationDates = new Hashtable<String, Long>();
	
	static {
		try { 
			gse = new GroovyScriptEngine(new String[] { "" });
		} catch (IOException e) {
			e.printStackTrace();
		} 
	} 
	
	public static boolean exists(String template) {
		return new File("target/classes/" + template + ".html").exists();
	}
	
	public static void render(String template, Map<String, Object> model, Writer writer) throws IOException, ResourceException, ScriptException {
		if (!template.startsWith("/")) {
			template = req().getController() + "/" + template;
		}
		new File("target/generated").mkdirs(); 
		if (template.contains("/")) {
			new File("target/generated/" + template.substring(0, template.lastIndexOf("/"))).mkdirs();
		}
		String groovySourceFile = generateSourceFile(template);
		Binding binding = new Binding();
		if (model != null) {
			for (String key : model.keySet()) {
				binding.setVariable(key, model.get(key));
			}
		}
		long started = System.currentTimeMillis();
		gse.run(groovySourceFile, binding);
		System.out.println("---- Time spent in rendering template " + template + " is " + (System.currentTimeMillis() - started)  + " ms");
	}

	private static String generateSourceFile(String template) 
			throws IOException, FileNotFoundException {
		String groovySourceFile = "target/generated/" + template + ".groovy";
		File file = new File("target/classes/" + template + ".html");
		if (!file.exists()) {
			throw new FileNotFoundException("Template " + template + ".html does not exist");
		}
		
		final String name = file.getName();
		Long date = filemodificationDates.get(name);
		long lastModified = file.lastModified();
		if (date == null || date < lastModified) {
			FileWriter generatedSourceWriter = new FileWriter(groovySourceFile);
			try {
				parse(file, generatedSourceWriter);
			} finally {
				generatedSourceWriter.close();
			} 
			filemodificationDates.put(name, lastModified);
		} 
		return groovySourceFile;
	}

	public static void main(String[] args) throws IOException, ResourceException, ScriptException {
		Map<String, Object> map = new HashMap<String, Object>();
		render("bank", map, new PrintWriter(System.out));
	}

	private static void parse(File file, Writer writer) throws FileNotFoundException, IOException {
		writer.append("import static framework.GlobalHelpers.*\n");
		writer.append("import static view.Helpers.*\n");
		writer.append("import entities.*\n");
		writer.append("import java.io.*\n");
		writer.append("import java.util.*\n");
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
					writer.append("data.out.write \"\" + (");
				} else if (s == 2) {
					s = 6;
					addChar(writer, s, ch);
				} else if (ch == '%' && s == 6) {
					s = 4;
				} else if (ch == '%' && s == 3) {
					s = 5;
				} else if (ch == '>' && s == 4) {
					s = 0;
					writer.append("\ndata.out.write \"");
				} else if (ch == '>' && s == 5) {
					s = 0;
					writer.append(")\ndata.out.write \"");
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
		if (ch == '\n') {
			if (s == 0) {
				writer.write("\\n");
			} else {
				writer.write(" ");
			}
		} else if (ch =='\r' ) { 
			if (s != 0) {
				writer.write(" ");
			}
		} else if (ch == '"' && s != 3 && s != 6) {
			writer.append("\\\"");
		} else if (ch == '$') {
			writer.append("\\$");
		} else {
			writer.write(ch);
		}
	}

}
