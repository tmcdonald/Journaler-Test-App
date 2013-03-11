package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import java.util.Date

import filters._

case class JournalEntry(id:Pk[Long] = NotAssigned, label:String, date: Date, comments: Option[String], owner: String)

object JournalEntry {

	val journalEntry = {
	  get[Pk[Long]]("id") ~
	  get[String]("label") ~
	  get[Date]("occurred") ~
	  get[Option[String]]("comments") ~
	  get[String]("owner") map {
	    case id~label~date~comments~owner => JournalEntry(id, label, date, comments, owner)
	  }
	}
	
	def all(username: String): List[JournalEntry] = DB.withConnection { implicit c =>
      SQL("select * from journalentry where owner={user}").on('user -> username).as(journalEntry *)
	}
	
	def create(entry: JournalEntry, username: String) {
	  create(entry.label,entry.date,entry.comments,username)
	}
	
	def create(label:String, date: Date, comments: Option[String], username: String) {
	  DB.withConnection { implicit c =>
	    SQL("insert into journalentry (label, occurred, comments, owner) values ({label}, {date}, {comments}, {owner})").on('label -> label, 'date -> date, 'comments -> comments, 'owner -> username).executeUpdate()
	  }
	}  
	
	def update(id:Long, entry: JournalEntry) {
	  update(id,entry.label,entry.date,entry.comments)
	}  
	
	def update(id:Long, label:String, date: Date, comments: Option[String]) {
	  DB.withConnection { implicit c =>
	    SQL("update journalentry set label = {label}, occurred = {date}, comments = {comments} where id = {id}").on('label -> label, 'date -> date, 'comments -> comments, 'id -> id).executeUpdate()
	  }
	}  
	
  	def delete(id:Long, user:String) {
 	  DB.withConnection { implicit c =>
	    SQL("delete from journalentry where id = {id} and owner={user}").on('id -> id, 'user -> user).executeUpdate()
	  } 	  
  	}
  	
  	def getById(id:Long, user:String) = DB.withConnection { implicit c =>
      SQL("select * from journalentry where id = {id} and owner={user}").on('id -> id, 'user -> user).as(journalEntry *).head
  	}
  	
  	def filter(filter:JournalEntryFilter, user: String): List[JournalEntry] =  DB.withConnection { implicit c =>
      SQL(JournalEntryFilter.buildQuery(filter, user)).as(journalEntry *)
	}
}
