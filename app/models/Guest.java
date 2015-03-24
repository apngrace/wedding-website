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
	
	public static String newGuestId() {
		return "new_guest";
	}
	
	public String weddingStr() {
		return getAttending(lastUpdateDate == null || attendingWedding);
	}
	
	public String rehearsalStr() {
		return getAttending(lastUpdateDate == null || attendingRehearsal);
	}
	
	public static String getAttending(boolean fieldValue) {
		if (fieldValue) {
			return ATTENDING;
		}
		return NOT_ATTENDING;
	}
	
	public static boolean getAttending(String value) {
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
		return Ebean.createQuery(Guest.class, "where household = ? and name != ? order by id").setParameter(1, guest.household).setParameter(2, guest.name).findList();
	}
	
	public static List<Guest> findRSVPedGuests() {
		return Ebean.createQuery(Guest.class, "where lastUpdateDate is not null order by lastUpdateDate DESC").findList();
	}
	
	public static List<Guest> findWaitingGuests() {
		return Ebean.createQuery(Guest.class, "where lastUpdateDate is null").findList();
	}
	
	public static Integer countWedding() {
		return Ebean.createSqlQuery("select count(attending_wedding) from guest where last_update_date is not null and attending_wedding = true").findUnique().getInteger("count");
	}
	
	public static Integer countRehearsal() {
		return Ebean.createSqlQuery("select count(attending_rehearsal) from guest where last_update_date is not null and attending_rehearsal = true").findUnique().getInteger("count");
	}
	
	public static Integer countWaiting() {
		return Ebean.createSqlQuery("select count(*) from guest where last_update_date is null").findUnique().getInteger("count");
	}
}
