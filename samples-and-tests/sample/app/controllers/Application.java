package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        // We're executing one HQL query here
        User user = User.find("byName", "Erik Bakker").first();
        
        render(user);
    }

}