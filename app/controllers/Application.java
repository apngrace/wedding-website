package controllers;

import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {
	public static String[] CONTENT_PAGES = new String[] {
		"index",
		"test"
	};
	

    public static Result index() {
        return ok(index.render("Charlotte and Archie's Wedding"));
    }
    
    public static Result test() {
    	return ok(render("main", "relfection Test", Html.apply("<p>Reflection Works Brah</p>")));
    }
    
    public static Result submit() {
    	return ok(test.render("Submit!!!"));
    }
    
    public static Result content(String action) {
    	//return ok(main.render(action, content.render()));
    	return ok(main.render(action, render("content")));
    }
    
    public static Result archie() {
    	return ok(main.render("test", Html.apply("<p>Archie's Page</p>")));
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
