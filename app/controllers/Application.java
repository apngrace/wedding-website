package controllers;

import models.Page;
import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {
	public static Page[] CONTENT_PAGES = new Page[] {
		new Page("Welcome", ""),
		new Page("About Us", "about"),
		new Page("Wedding Events", "events"),
		new Page("Travel", "travel"),
		new Page("Accommodations", "accommodations"),
		new Page("Local Activities", "activities"),
		new Page("Registry", "registry"),
		new Page("Contact Us", "contact"),
		new Page("RSVP", "rsvp")
	};
    
    public static Result content(String action) {
    	Page current = getCurrentPage(action);
    	Html content;
    	
    	if (action.isEmpty()) {
    		content = render("index");
    	} else {
    		content = render(action);
    	}
    	
    	return ok(main.render(current, CONTENT_PAGES, content));
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
