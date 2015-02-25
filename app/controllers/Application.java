package controllers;

import models.Guest;
import models.Page;
import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {
	public static final String AUTH_TIME = "authTime";
	public static final long AUTH_TIMEOUT = 30 * 60 * 1000; //30 Minutes
	public static final String PASSWORD = "springwedding";
	
	private static final Page RSVP_PAGE = new Page("RSVP", "rsvp");
	
	public static Page AUTH = new Page("Password", "");
	
	public static Page[] CONTENT_PAGES = new Page[] {
		new Page("Welcome", ""),
		new Page("About Us", "about"),
		new Page("Wedding Events", "events"),
		new Page("Travel", "travel"),
		new Page("Accommodations", "accommodations"),
		new Page("Local Activities", "activities"),
		new Page("Registry", "registry"),
		new Page("Contact Us", "contact"),
		RSVP_PAGE
	};
    
    public static Result content(String action) {
    	
    	if (!checkAuth()) {
    		return auth();
    	}
    	
    	Page current = getCurrentPage(action);
    	Html content;
    	
    	if (action.isEmpty()) {
    		content = render("index");
    	} else if (current == RSVP_PAGE) {
    		content = render(action, Guest.findAll());
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
    		return content("");
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
