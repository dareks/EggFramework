package controllers;

import static framework.GlobalHelpers.*;
import static view.Helpers.*;

import java.util.Iterator;

import org.bson.types.ObjectId;

import com.google.code.morphia.query.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import entities.Player;
import framework.ClientException;
import framework.Redirect;
import framework.Response;
import framework.validation.Errors;
import services.Application;

public class IndexController {

	Application app;
	
	public void before() {
	}
	
	static {
		validateParams("/bank", SomeForm.class); 
//		validateParam("/bank", "id", requiredValidator);
	}
	
	public void bank() {
		app.db.getCollection("players").drop();
		app.datastore.save(new Player());
		Player next = app.datastore.find(Player.class).iterator().next();
    	SomeForm form = paramsAsBean(SomeForm.class);
    	System.out.println(">>>> " + form.id);
    	System.out.println(">>>> " + form.age);
    	// przykladowa akcja.. Gosciu wybiera fabrykę która chce rozbudować
    	// ID fabryki. Po walidacji (czy ID jest podane i jest liczbą) następuje sprawdzenie 
    	// czy fabryka istnieje (tak naprawde trzeba pobrac player a pozniej rzuc wyjatek gdy nie istnieje)
    	System.out.println(app.get(Player.class, next.id.toString()));
//    	throw new RuntimeException("SOME EXCEPTION");
	}
	
	public Response redirectTest() {
		flash("messages", ImmutableList.of("Redirected from redirectTest"));
		return redirect().action("index");
	}

}
 