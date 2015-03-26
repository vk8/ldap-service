package controllers

import play.api._
import play.api.mvc._
import java.util.Hashtable
import javax.naming.Context
import javax.naming.ldap.InitialLdapContext

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}