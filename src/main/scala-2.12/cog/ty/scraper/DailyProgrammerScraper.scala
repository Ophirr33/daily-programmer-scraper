package cog.ty.scraper

import com.typesafe.scalalogging
import java.io.{BufferedWriter, File}
import java.net.URL
import java.util.Date

import com.typesafe.scalalogging.Logger
import net.dean.jraw.RedditClient
import net.dean.jraw.http.UserAgent
import net.dean.jraw.http.oauth.{Credentials, OAuthHelper}
import net.dean.jraw.models.{
  Account,
  Comment,
  CommentNode,
  Listing,
  Submission
}
import net.dean.jraw.paginators.{Sorting, SubredditPaginator, TimePeriod}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by ty on 3/12/17.
  */
class DailyProgrammerScraper {}

object Demo extends App {
  val logger = Logger("Demo")
  val userAgent = UserAgent.of("desktop", "cog.ty", "v0.1", "NiceGuy_Ty")
  val classLoader = getClass.getClassLoader
  val credentialsFile = new File(
    classLoader.getResource("credentials.conf").getFile)
  val credentials =
    Source.fromFile(credentialsFile).getLines().next().split(" ")
  val scriptCredentials = Credentials.script(credentials(0),
                                             credentials(1),
                                             credentials(2),
                                             credentials(3))
  val redditClient = new RedditClient(userAgent)
  val authData = redditClient.getOAuthHelper.easyAuth(scriptCredentials)
  redditClient.authenticate(authData)
  logger.info(redditClient.me().hasVerifiedEmail.toString)
  logger.info(redditClient.getCurrentRatelimit.toString)

  val dailyProgrammer = new SubredditPaginator(redditClient, "dailyprogrammer")
  dailyProgrammer.setTimePeriod(TimePeriod.ALL)
  dailyProgrammer.setSorting(Sorting.NEW)
  dailyProgrammer.setLimit(100)

  import collection.JavaConverters._

  val users = ArrayBuffer[User]()
  val challenges = ArrayBuffer[Challenge]()
  val responses = ArrayBuffer[Response]()

  while (dailyProgrammer.hasNext) {
    val submissions: Listing[Submission] = dailyProgrammer.next()

    for ((s: Submission) <- submissions.iterator().asScala) {
      val validChallenge =
        """\[\d+(-|/)\d+(-|/)\d+] Challenge #\d+ \[(\w+)].*""".r
      s.getTitle match {
        case validChallenge(_, _, difficulty) =>
          logger.debug(s"Successfully matched regex for `${s.getTitle}`")
          parseSubmission(s, Difficulty(difficulty)).foreach {
            case (challenge, user, response) =>
              challenges += challenge
              users += user
              responses += response
              logger.debug("Successfully parsed a response with challenge id"
                + s"`${challenge.id}`, user id `${user.id}`, and response id `${response.id}`")
          }
        case _ =>
          logger.debug(
            s"Could not parse submission with title `${s.getTitle}`")
      }
    }
  }

  val bw = new BufferedWriter(
    new java.io.FileWriter(
      new File(classLoader.getResource("out.txt").getFile),
      false))

  bw.write("")
  bw.append("CHALLENGES")
  bw.append("-------------")
  bw.append(challenges.map(ch => ch.id + ": " + ch.title).mkString("\n"))
  bw.append("USERS")
  bw.append("-------------")
  bw.append(users.map(us => us.id + ": " + us.username).mkString("\n"))
  bw.append("RESPONSES")
  bw.append("-------------")
  bw.append(
    responses
      .map(r =>
        s"${r.challenge.id} -> ${r.user.id} -> ${r.id}:"
          + s"${r.body.substring(0, math.min(r.body.length, 10))}")
      .mkString("\n"))
  bw.flush()
  bw.close()

  def parseSubmission(
      s: Submission,
      difficulty: Difficulty): Iterable[(Challenge, User, Response)] = {
    val challenge = Challenge(s.getId,
                              s.getCreated,
                              permaLinkToUrl(s.getPermalink),
                              s.getTitle,
                              s.getSelftext,
                              difficulty)
    val comments = s.getComments
    logger.info((comments == null).toString)
    for {
      (c: CommentNode) <- if (comments != null) comments.walkTree().asScala else Iterable.empty[CommentNode]
    } yield {
      val comment = c.getComment
      val user =
        User(redditClient.getUser(comment.getAuthor).getId, comment.getAuthor)
      val response = Response(comment.getId,
                              challenge,
                              user,
                              comment.getCreated,
                              new URL(comment.getUrl),
                              comment.getBody)
      (challenge, user, response)
    }
  }

  def permaLinkToUrl(relativePath: String): URL =
    new URL("https://reddit.com/" + relativePath)
}

case class User(id: String, username: String)

case class Challenge(id: String,
                     created: Date,
                     link: URL,
                     title: String,
                     body: String,
                     difficulty: Difficulty)

case class Response(id: String,
                    challenge: Challenge,
                    user: User,
                    created: Date,
                    link: URL,
                    body: String)

sealed trait Difficulty

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
