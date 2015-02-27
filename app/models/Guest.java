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
	@Id
	public long id;
	public String name;
	public String household;
	public boolean attendingWedding;
	public boolean attendingRehearsal;
	public Date lastUpdateDate; 
	
	public static List<Guest> findAll() {
		return Ebean.find(Guest.class).findList();
	}
	
	public static List<Guest> findByName(String name) {
		return Ebean.createQuery(Guest.class, "where lower(name) like lower(?)").setParameter(1, "%" + name + "%").findList();
	}
	
	public static List<Guest> findOtherHouseholdGuests(Guest guest) {
		return Ebean.createQuery(Guest.class, "where household = ? and name != ?").setParameter(1, guest.household).setParameter(2, guest.name).findList();
	}
}
