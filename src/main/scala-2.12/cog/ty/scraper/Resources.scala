package cog.ty.scraper

import java.net.URL
import java.util.Date

import net.dean.jraw.models.Submission

/**
  * The resources we care about from Daily Programming.
  * Designed to be easy to write to a database
  * @author ophirr33
  * @since 3/14/17
  */
case class User(username: String)

case class Challenge(id: String,
                     created: Date,
                     link: URL,
                     title: String,
                     body: String,
                     difficulty: Difficulty)

object Challenge {
  def apply(s: Submission, difficulty: Difficulty): Challenge =
    Challenge(s.getId,
              s.getCreated,
              new URL("https://reddit.com/" + s.getPermalink),
              s.getTitle,
              s.getSelftext,
              difficulty)
}

case class Response(id: String,
                    challenge: Challenge,
                    user: User,
                    created: Date,
                    link: Option[URL],
                    body: String)

sealed trait Difficulty {
  val difficulty: String = this match {
    case Easy => "easy"
    case Intermediate => "intermediate"
    case Hard => "hard"
    case Other(tag) => tag
  }
}

case object Easy extends Difficulty

case object Intermediate extends Difficulty

case object Hard extends Difficulty

case class Other(tag: String) extends Difficulty

object Difficulty {
  def apply(str: String): Difficulty = str.toLowerCase match {
    case "easy" => Easy
    case "intermediate" => Intermediate
    case "hard" => Hard
    case _ => Other(str.toLowerCase)
  }
}
