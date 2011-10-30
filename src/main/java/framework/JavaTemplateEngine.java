package framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;

public class JavaTemplateEngine implements TemplateEngine {

    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;

    // Cache of pages for production mode
    private ConcurrentHashMap<String, Page> pages = new ConcurrentHashMap<String, Page>();

    public JavaTemplateEngine() {
        compiler = ToolProvider.getSystemJavaCompiler();
        fileManager = compiler.getStandardFileManager(null, null, null); // TODO Add encoding!

        // fileManager.close();
    }

    public void generate(String name, Reader reader, File outputFile) throws FileNotFoundException, IOException {
        generateFirstPhase(name, reader, outputFile);
        reader = new StringReader(IOUtils.toString(new FileReader(outputFile)));
        FileWriter writer = new FileWriter(outputFile);
        try {
            int s = 0;
            int ch = -1;
            StringBuilder directiveOrArg = new StringBuilder();
            Stack<DirectiveStackElement> stack = new Stack<DirectiveStackElement>();
            while ((ch = reader.read()) != -1) {
                if (ch == '[') {
                    directiveOrArg = new StringBuilder();
                    s = 1;
                } else if (ch == ']') {
                    stack.peek().addArgument(directiveOrArg);
                    directiveOrArg = new StringBuilder();

                    DirectiveStackElement element = stack.pop();
                    String code = generateDirectiveCode(element.directive, element.args);
                    if (stack.isEmpty()) {
                        writer.append(code);
                        s = 0;
                    } else {
                        stack.peek().addArgument(new StringBuilder(code));
                    }
                } else if (s > 0) {
                    directiveOrArg.append((char) ch);
                    if (ch == ' ' || ch == '\n') {
                        if (s == 1) {
                            stack.push(new DirectiveStackElement(Directive.parse(loadDirective(directiveOrArg.toString().trim()))));
                            directiveOrArg = new StringBuilder();
                        } else if (s > 1) {
                            stack.peek().addArgument(directiveOrArg);
                            directiveOrArg = new StringBuilder();
                        }
                        s = 2;
                    }
                } else if (s == 0) {
                    writer.write(ch);
                }
            }
        } finally {
            reader.close();
            writer.close();
        }
    }

    private String generateDirectiveCode(Directive directive, List<StringBuilder> args) throws IOException {
        String code = directive.code;
        for (int t = 0; t < args.size(); t++) {
            code = code.replace("$" + t, args.get(t));
        }
        return code;
    }

    public String getFileExtension() {
        return "java";
    }

    public void run(String template, String file, Map<String, Object> model) throws Exception {
        Page page = null;
        if (pages.containsKey(template)) { // TODO THIS CACHE SHOULD BE USED ONLY FOR PRODUCTION
            page = pages.get(template);
        } else {
            Iterable<? extends JavaFileObject> objects = fileManager.getJavaFileObjects(new File(file));
            compiler.getTask(null, fileManager, null, null, null, objects).call();

            File classesDir = new File("target/generated"); // TODO SECOND PLACE WHERE THIS PATH IS SPECIFIED!
            URL[] urls = new URL[] { classesDir.toURL() };
            URLClassLoader ucl = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            String className = template.replace('/', '.').substring(1);
            Class<Page> clazz = (Class<Page>) ucl.loadClass(className);

            page = clazz.newInstance();
            pages.put(template, page);
        }
        page.render(model);
    }

    public String loadDirective(String name) throws IOException {
        if (getClass().getResource("/directives/" + name + ".java") == null) {
            throw new FileNotFoundException("Directive " + name + " not found");
        }
        InputStream in = getClass().getResourceAsStream("/directives/" + name + ".java");
        return IOUtils.toString(in); // TODO use encoding
    }

    public void generateFirstPhase(String name, Reader reader, File outputFile) throws FileNotFoundException, IOException {
        List<String> imports = IOUtils.readLines(Template.class.getResourceAsStream("/imports.java"));
        FileWriter writer = new FileWriter(outputFile);
        try {
            writer.append("package ").append(name.substring(1, name.lastIndexOf('/'))).append(";\n");

            for (String line : imports) {
                writer.append(line).append('\n');
            }
            writer.append("public class ").append(name.substring(name.lastIndexOf('/') + 1)).append(" implements Page {\n");
            writer.append("public void render(Map<String, Object> model) throws Exception { ");
            writer.append("framework.ThreadData data = framework.FrontController.threadData.get();\n");

            writer.append("final String content = (String) model.get(\"content\");\n");
            writer.append("final Errors errors = (Errors) model.get(\"errors\");\n");

            writer.append("Writer out = data.getOut();\n");

            int s = 0;
            int ch = -1;
            writer.append("out.write(\"");
            while ((ch = reader.read()) != -1) {
                if (ch == '<' && s == 0) {
                    s = 1;
                    writer.append("\");\n");
                } else if (ch == '%' && s == 1) {
                    s = 2;
                } else if (s == 1) {
                    s = 0;
                    writer.append("out.write(\"<");
                    addChar(writer, s, ch);
                } else if (ch == '=' && s == 2) {
                    s = 3;
                    writer.append("out.write(\"\" + (");
                } else if (s == 2) {
                    s = 6;
                    addChar(writer, s, ch);
                } else if (ch == '%' && s == 6) {
                    s = 4;
                } else if (ch == '%' && s == 3) {
                    s = 5;
                } else if (ch == '>' && s == 4) {
                    s = 0;
                    writer.append("\nout.write(\"");
                } else if (ch == '>' && s == 5) {
                    s = 0;
                    writer.append("));\nout.write(\"");
                } else if (s == 4) {
                    s = 2;
                    writer.append('%');
                } else if (s == 5) {
                    s = 3;
                    writer.append('%');
                } else if (ch == '\n') {
                    if (s == 0) {
                        writer.append("\\n\");\nout.write(\"");
                    } else {
                        writer.append(" ");
                    }
                    s = 0;
                } else if (ch == '\r') {
                    if (s != 0) {
                        writer.append(" ");
                    }
                } else {
                    addChar(writer, s, ch);
                }
            }
            if (s == 0) {
                writer.append("\");");
            }
            writer.append("}}");
        } finally {
            reader.close();
            writer.close();
        }
    }

    private static void addChar(Writer writer, int s, int ch) throws IOException {
        if (ch == '"' && s != 3 && s != 6) {
            writer.append("\\\"");
        } else if (ch == '\\') {
            writer.append("\\\\");
        } else if (ch == '$') {
            writer.append("\\$");
        } else {
            writer.write(ch);
        }
    }

    public static class Directive {

        public final String code;
        public final int argsCount;

        public Directive(String code, int argsCount) {
            this.code = code;
            this.argsCount = argsCount;
        }

        public static Directive parse(String code) throws NumberFormatException, IOException {
            int argsCount = 0;
            int ch = -1;
            int s = 0;
            StringReader reader = new StringReader(code);
            StringBuilder arg = new StringBuilder();
            while ((ch = reader.read()) != -1) {
                if (ch == '$' && s == 0) {
                    s = 1;
                } else if (s == 1 && (!Character.isDigit((char) ch))) {
                    if (ch != '$') {
                        Integer no = Integer.valueOf(arg.toString());
                        argsCount = Math.max(argsCount, no + 1);
                    }
                    arg = new StringBuilder();
                    s = 0;
                } else if (s == 1) {
                    arg.append((char) ch);
                }
            }
            return new Directive(code, argsCount);
        }

    }

    public static class DirectiveStackElement {
        public final Directive directive;
        private final List<StringBuilder> args;

        public DirectiveStackElement(Directive directive) {
            this.directive = directive;
            this.args = Lists.newArrayList();
        }

        public void addArgument(StringBuilder arg) {
            if (args.size() == directive.argsCount) {
                args.get(args.size() - 1).append(arg);
            } else {
                args.add(arg);
            }
        }

    }
}
