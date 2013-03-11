package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class User(username: String, password: String)

object User {
  
  val user = {
    get[String]("user.username") ~
    get[String]("user.password") map {
      case username~password => User(username, password)
    }
  }
  
  def all: List[User] = {
    DB.withConnection { implicit c =>
      SQL("select * from user").as(user *)
    }
  }
  
  def authenticate(username: String, password: String): Option[User] = {
    DB.withConnection { implicit c =>
      SQL("select * from user where username = {username} and password = {password}").on('username -> username, 'password -> password).as(user.singleOpt)
    }
  }
   
  def create(user: User): User = {
    create(user.username, user.password)
    user
  }
  
  def create(username:String, password:String) = {
    DB.withConnection { implicit c =>
      SQL("insert into user (username, password) values ({username}, {password})").on('username -> username, 'password -> password).executeUpdate()
    }
  }
  
  def userExists(username: String): Boolean = {
    DB.withConnection { implicit c =>
      (SQL("select count(*) from user where username = {username}").on('username -> username).as(scalar[Long].single) > 0)
    }
  }
}