package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._
import java.util.Date

import models._

case class UserRegistration(username:String,password:String,passwordAgain:String)

object Authentication extends Controller {

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    ) verifying ("Invalid username or password", result => result match {
      case (username, password) => User.authenticate(username, password).isDefined
    })
  )
  val createUserForm = Form(
    mapping(
      "username" -> nonEmptyText.verifying( "Username already exists.", username => User.userExists(username) == false ),
      "password" -> nonEmptyText,
      "passwordAgain" -> nonEmptyText
    )(UserRegistration.apply)(UserRegistration.unapply)
    verifying("Passwords did not match.", form => form.password == form.passwordAgain)
  )
  
  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.Application.index).withSession(Security.username -> user._1)
    )
  }
  
  def newUser = Action { implicit request =>
    Ok(views.html.createUser(createUserForm))
  }

   def createNewUser = Action { implicit request =>
    createUserForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createUser(formWithErrors)),
      userRegistration => {
    	  User.create(userRegistration.username, userRegistration.password)
    	  Ok(views.html.login(loginForm))
        }
    )
  }
 
  def logout = Action {
    Redirect(routes.Authentication.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }
}

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.login)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
  
}