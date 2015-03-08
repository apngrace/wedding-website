package controllers;

import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.common.BeanList;

import models.Guest;
import models.Page;
import play.Logger;
import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {
	public static final String AUTH_TIME = "authTime";
	public static final long AUTH_TIMEOUT = 30 * 60 * 1000; //30 Minutes
	public static final String PASSWORD = "charlotteishot";
	
	private static final Page RSVP_PAGE = new Page("RSVP", "rsvp");
	private static final Page ADMIN_PAGE = new Page("Admin", "admin");
	
	public static Page AUTH = new Page("Password", "");
	
	public static Page[] CONTENT_PAGES = new Page[] {
		new Page("Welcome", ""),
		new Page("About Us", "about"),
		new Page("Wedding Events", "events"),
		new Page("Travel", "travel"),
		new Page("Accommodations", "accommodations"),
		new Page("Local Activities", "activities"),
		new Page("Registry", "registry"),
		new Page("Contact Us", "contact")
	};
	
	public static Result rsvpSubmit(String term) {
		Map<String, String[]> fields = request().body().asFormUrlEncoded();
		
		String[] ids = fields.get("id"), names = fields.get("name");
		List<Guest> guests = new BeanList<Guest>();
		int name_index = 0;
		for(String id : ids) {
			Guest g;
			if (id.equals(Guest.newGuestId())) {
				g = new Guest();
				g.name = names[name_index++];
				if (guests.size() == 0) {
					throw new IllegalStateException("Adding guest at start of RSVP...");
				}
				g.household = guests.get(0).household;
				g.id = Ebean.createSqlQuery("select nextval('guest_id_seq')").findUnique().getLong("nextval");
			} else {
				g = Guest.findById(id);
			}
			if (g == null) {
				Logger.warn("No guest found for id: " + id);
			}
			guests.add(g);
		}
		
		String[] wedding = fields.get("attendingWedding"), rehearsal = fields.get("attendingRehearsal");
		
		for (int i = 0; i < guests.size(); i++) {
			Guest g = guests.get(i);
			if (g == null) { continue; }
			g.attendingWedding = Guest.getAttending(wedding[i]);
			g.attendingRehearsal = Guest.getAttending(rehearsal[i]);
			g.lastUpdateDate = new Date();
			g.save();
		}
		
		Page current = RSVP_PAGE;
		Html content = render(current.link, guests, term, false, true, false);
		return ok(main.render(current, new Page[0], content));
	}
	
	public static Result rsvp(String term) {
		try {
			term = URLDecoder.decode(term, "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
			return content(RSVP_PAGE.link);
		}
		
		Page current = RSVP_PAGE;
		
		boolean needsVerification = false;
		List<Guest> guests = Guest.findByName(term);
		if (guests.size() == 0) {
			guests = Guest.findByName(term.replace(" ", "%"));
		}
		
		if (guests.size() > 1) {
			String household = guests.get(0).household;
			for (int i = 0;i<guests.size(); i++) {
				if (!household.equals(guests.get(i))) {
					needsVerification = true;
					break;
				}
			}
		}
		boolean alreadySubmitted = (guests.size() > 0);
		for (Guest g : guests) {
			if (g.lastUpdateDate == null) {
				alreadySubmitted = false;
				break;
			}
		}
		
		if (!needsVerification && !guests.isEmpty()) {
			guests.addAll(Guest.findOtherHouseholdGuests(guests.get(0)));
		}
		
		Html content = render(current.link, guests, term, needsVerification, false, !(needsVerification) && alreadySubmitted);
		return ok(main.render(current, new Page[0], content));
	}
	
	public static Result rsvp() {
		Html content = render("rsvp", new BeanList<Guest>(), "", false, false, false);
		return ok(main.render(RSVP_PAGE, new Page[0], content));
	}
    
	public static Result admin() {
		Html content = render("admin", Guest.findRSVPedGuests(), Guest.countRehearsal(), Guest.countWedding());
		return ok(main.render(ADMIN_PAGE, new Page[0], content));
	}
    
    public static Result content(String action) {
    	if (action.equals(RSVP_PAGE.link) || action.isEmpty()) {
    		return rsvp();
    	}
    	
    	if (!checkAuth()) {
    		return auth();
    	}

    	if (action.endsWith("/")) {
    		action = action.substring(0, action.length() - 1);
    	}

    	if (action.equals(ADMIN_PAGE.link)) {
    		return admin();
    	}

    	Page current = getCurrentPage(action);
    	Html content;
    	if (action.isEmpty()) {
    		content = render("index");
    	} else if (current == RSVP_PAGE) {
    		content = render(action, new BeanList<Guest>(), "", false, false, false);
    	} else {
    		content = render(action);
    	}
    	
    	return ok(main.render(current, CONTENT_PAGES, content));
    }
    
    private static Result auth() {
    	return ok(main.render(AUTH, new Page[0], render("auth"))); 
    }
    
    public static Result login() {
    	String[] passwords = request().body().asFormUrlEncoded().get("password");
    	
    	if (passwords == null || passwords.length != 1 || !passwords[0].equals(PASSWORD)) {
    		return auth();
    	} else {
    		authorizeUser();
    		return redirect("/admin");
    	}
    }
    
    private static void authorizeUser() {
    	session(AUTH_TIME, Long.toString(System.currentTimeMillis() + AUTH_TIMEOUT));
    }
    
    private static boolean checkAuth() {
    	String temp = session(AUTH_TIME);
    	if (temp == null) {
    		return false;
    	}
    	
    	Long authTime = null;
    	try {
    		
    		authTime = Long.parseLong(session(AUTH_TIME));
    	} catch (NumberFormatException ex) {
    		ex.printStackTrace();
    		authTime = null;
    	}
    	
    	if (authTime == null || authTime < System.currentTimeMillis()) {
    		return false;
    	}
    	
    	return true;
    }
    
    private static Page getCurrentPage(String link) {
    	for (Page p: CONTENT_PAGES) {
    		if (link.equals(p.link)) {
    			return p;
    		}
    	}
    	return null;
    }
    
    private static Html render(String page, Object... params) {
    	Class<?>[] classes = new Class<?>[params.length];
    	for (int i = 0; i < params.length; i++) {
    			classes[i] = params[i].getClass();
    	}
    	
    	try {
	    	final Class<?> clazz = Class.forName("views.html." + page);
	    	java.lang.reflect.Method render = clazz.getDeclaredMethod("render", classes);
	    	Html html = (play.api.templates.Html) render.invoke(null, params);
	    	return html;
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		return Html.apply("Error Loading Content: " + page + ": " + ex.getMessage());
    	}
    }

}
