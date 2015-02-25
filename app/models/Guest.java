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
}
