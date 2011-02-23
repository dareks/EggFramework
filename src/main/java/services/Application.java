package services;

import static framework.GlobalHelpers.*;

import java.net.UnknownHostException;

import org.bson.types.ObjectId;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.DB;
import com.mongodb.Mongo;

import framework.ClientException;

public class Application {

	public Morphia morphia;
	public Mongo mongo;
	public DB db;
	public Datastore datastore;
	
	public void start() throws Exception {
		System.out.println("Starting...");
		startMongo();
	} 

	private void startMongo() throws UnknownHostException, ClassNotFoundException {
		mongo = new Mongo();
		db = mongo.getDB("industriality");
		morphia = new Morphia(); 
		morphia.mapPackage("entities"); 
 		datastore = morphia.createDatastore(mongo, "industriality");
	} 
	
	public void stop() {
		System.out.println("Stopping...");
		stopMongo();
	}

	private void stopMongo() {
		mongo.close();
	}
	
	// MONGO FUNCTIONS (extend Datastore instead)
	public boolean exists(Class<?> clazz, String id) {
		if(!ObjectId.isValid(id)) {
			return false;
		}
		return datastore.get(clazz, new ObjectId(id)) != null;
	}
	
	/** Throw an exception when row does not exist */
	public <T> T get(Class<T> clazz, String id) {
		if(!ObjectId.isValid(id)) {
			throw new ClientException(f("%s with id = %s does not exist", clazz.getName(), id));
		}
		T x = datastore.get(clazz, new ObjectId(id));
		if (x == null) {
			throw new ClientException(f("%s with id = %s does not exist", clazz.getName(), id));
		}
		return x;
	}

}
 