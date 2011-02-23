package view;

import static framework.GlobalHelpers.*;
import framework.FrontController;
import groovy.lang.Closure;

import java.io.IOException;

import org.bson.types.ObjectId;

/**
 * This class is automatically reloaded on every change
 */
public class Helpers {

	public static void closureExample(Closure closure) throws IOException {
		out("Mega wyczes sposób żeby zapisywać do writera! Lepsze od uzywania return szczegolnie gdy chcemy wiecej niz jeden teskst zwrocic! Jednak dzialac bedzie tylko w skryptletach !");
		closure.call();
		out("<br />");
	}
	
	public static CharSequence popup(String file, String title, Closure closure) {
		return parse("_popup", map("file", file, "title", title, "body", call(closure)));
	}

}
