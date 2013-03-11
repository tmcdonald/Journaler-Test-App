package filters

import java.util.Date
import java.text.SimpleDateFormat

case class JournalEntryFilter(label:Option[String], dateFrom: Option[Date], dateTo: Option[Date], comments: Option[String])

trait JournalFilterItem {
  def conditional: String
}

case class DateToJournalFilter(date: Option[Date]) extends JournalFilterItem {
  def conditional: String = date match {
    case None => ""
    case Some(value) => "occurred < '" + new SimpleDateFormat("yyyy-MM-dd").format(value) + "'"
  }
}

case class DateFromJournalFilter(date: Option[Date]) extends JournalFilterItem {
  def conditional: String = date match {
    case None => ""
    case Some(value) => "occurred > '" + new SimpleDateFormat("yyyy-MM-dd").format(value) + "'"
  }
}

case class LabelJournalFilter(label: Option[String]) extends JournalFilterItem {
  def conditional: String = label match {
    case None => ""
    case Some(value) => "label like '%" + value + "%'"
  }
}

case class CommentsJournalFilter(comments: Option[String]) extends JournalFilterItem {
  def conditional: String = comments match {
    case None => ""
    case Some(value) => "comments like '%" + value + "%'"
  }
}

case class UserJournalFilter(user: String) extends JournalFilterItem {
    def conditional: String ="owner = '" + user + "'"
}

object JournalEntryFilter {
    
  def buildQuery(filter: JournalEntryFilter, user: String): String = {
    val filterItems = List(new LabelJournalFilter(filter.label), new CommentsJournalFilter(filter.comments), new DateToJournalFilter(filter.dateTo), new DateFromJournalFilter(filter.dateFrom),new UserJournalFilter(user))
    val where = filterItems.filter(_.conditional.length > 0).map(f => f.conditional) mkString " AND "

    "select * from journalentry where " + where
  }
  
}