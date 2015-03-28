package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import play.twirl.api.Html

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def allSortedLdapAttributes = Action {
    Ok(Html(LdapUtils.usefulAttributesFinder.toString))
  }
  
}