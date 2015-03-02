package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.Ebean;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class Guest extends Model {
	private static final String ATTENDING = "Will Attend";
	private static final String NOT_ATTENDING = "Won't Attend";
	
	@Id
	public long id;
	public String name;
	public String household;
	public boolean attendingWedding;
	public boolean attendingRehearsal;
	public Date lastUpdateDate;
	
	public String weddingStr() {
		return getAttending(lastUpdateDate == null || attendingWedding);
	}
	
	public String rehearsalStr() {
		return getAttending(lastUpdateDate == null || attendingRehearsal);
	}
	
	public String getAttending(boolean fieldValue) {
		if (fieldValue) {
			return ATTENDING;
		}
		return NOT_ATTENDING;
	}
	
	public boolean getAttending(String value) {
		if (ATTENDING.equals(value)) {
			return true;
		} else if (NOT_ATTENDING.equals(value)) {
			return false;
		} else {
			throw new IllegalArgumentException("Unrecognized value: " + value);
		}
	}
	
	public static List<Guest> findAll() {
		return Ebean.find(Guest.class).findList();
	}
	
	public static Guest findById(String id) {
		return Ebean.find(Guest.class, id);
	}
	
	public static List<Guest> findByName(String name) {
		return Ebean.createQuery(Guest.class, "where lower(name) like lower(?)").setParameter(1, "%" + name + "%").findList();
	}
	
	public static List<Guest> findOtherHouseholdGuests(Guest guest) {
		return Ebean.createQuery(Guest.class, "where household = ? and name != ?").setParameter(1, guest.household).setParameter(2, guest.name).findList();
	}
}
