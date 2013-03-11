package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import anorm._
import java.util.Date

import models._
import filters._

object Application extends Controller with Secured {
  
  val entryForm = Form(
    tuple (
      "id" -> ignored(NotAssigned:Pk[Long]),
      "label" -> nonEmptyText,
      "date" -> date("yyyy-MM-dd"),
      "comments" -> optional(text)
    )
  )
  
  val entrySearchForm = Form(
    mapping(
      "label" -> optional(text),
      "dateFrom" -> optional(date("yyyy-MM-dd")),
      "dateTo" -> optional(date("yyyy-MM-dd")),
      "comments" -> optional(text)
    )(JournalEntryFilter.apply)(JournalEntryFilter.unapply)
  )
  
  def index = withAuth { username => implicit request =>
    Redirect(routes.Application.journaler)
  }
  
  def journaler = withAuth { username => implicit request =>
    Ok(views.html.index())
  }
  
  def newEntry(id:Long) = withAuth { username => implicit request =>
    id match{
      case 0 => Ok(views.html.entry(None,entryForm))
      case _ => {
    	  val entry = JournalEntry.getById(id, username)
    	  Ok(views.html.entry(Some(entry),entryForm.fill(entry.id, entry.label, entry.date, entry.comments)))
        }
    }
  }
  
  def createUpdateEntry(id:Long) = withAuth { username => implicit request =>
    entryForm.bindFromRequest.fold(
      errors => {
        val returnEntry = id match {
              case 0 => None
              case _ => Some(JournalEntry.getById(id, username))
            }
        BadRequest(views.html.entry(returnEntry, errors))
      },
      entry => {
        id match {
          case 0 => JournalEntry.create(entry._2,entry._3,entry._4, username)
          case _ => JournalEntry.update(id, entry._2,entry._3,entry._4)
        }
        Redirect(routes.Application.list)
      }
    )
  }
  
  def deleteEntry(id:Long) =  withAuth { username => implicit request =>
    JournalEntry.delete(id, username)
    Redirect(routes.Application.list)
  }

  def list() = withAuth { username => implicit request =>
    Ok(views.html.listEntries(JournalEntry.all(username)))
  }
  
  def search = Action {implicit request =>
    Ok(views.html.search(entrySearchForm))
  }
  
  def searchResults = withAuth { username => implicit request =>
    entrySearchForm.bindFromRequest.fold(
      errors => BadRequest(views.html.search(errors)),
      search => Ok(views.html.listEntries(JournalEntry.filter(search, username)))
    )
  }
}
