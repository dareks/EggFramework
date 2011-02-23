package entities;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * Encje Mongo się przeładowują bez problemu :)
 */
@Entity("players")
public class Player {
	@Id
	public ObjectId id;
	public String uid;
	public String company;
	
	@Override
	public String toString() {
		return "Player [id=" + id + ", uid=" + uid + ", company=" + company + "]";
	}

}
