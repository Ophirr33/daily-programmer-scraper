package cog.ty.scraper

import com.typesafe.scalalogging
import java.io.File
import java.net.URL
import java.util.Date

import com.typesafe.scalalogging.Logger
import net.dean.jraw.RedditClient
import net.dean.jraw.http.UserAgent
import net.dean.jraw.http.oauth.{Credentials, OAuthHelper}
import net.dean.jraw.models.{Listing, Submission}
import net.dean.jraw.paginators.{Sorting, SubredditPaginator, TimePeriod}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by ty on 3/12/17.
  */
class DailyProgrammerScraper {

}

object Demo extends App {
  val logger = Logger("Demo")
  val userAgent = UserAgent.of("desktop", "cog.ty", "v0.1", "NiceGuy_Ty")
  val classLoader = getClass.getClassLoader
  val credentialsFile = new File(classLoader.getResource("credentials.conf").getFile)
  val credentials = Source.fromFile(credentialsFile).getLines().next().split(" ")
  val scriptCredentials = Credentials.script(credentials(0), credentials(1), credentials(2), credentials(3))
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
  while (dailyProgrammer.hasNext) {
    val users = ArrayBuffer[User]()
    val challenges = ArrayBuffer[Challenge]()
    val challengeResponses = ArrayBuffer[ChallengeResponse]()
    val submissions: Listing[Submission] = dailyProgrammer.next()
    for ((s: Submission) <- submissions.iterator().asScala) {
      val validChallenge = """\[(\d\d\d\d)(-|/)(\d\d)(-|/)(\d\d)\] Challenge #(\d)+ \[w+\]""".r
      s.getTitle match {
        case validChallenge(_*) =>
          logger.debug(s"Successfully matched regex for `${s.getTitle}`")
          parseSubmission(s, users, challenges, challengeResponses)
        case _ =>
          logger.debug(s"Could not parse submission with title `${s.getTitle}`")
      }
    }
  }

  def parseSubmission(s: Submission, users: ArrayBuffer[User], challenges: ArrayBuffer[Challenge], challengeResponses: ArrayBuffer[ChallengeResponse]) = {
    val challenge = Challenge(s.getId, s.getCreated, permaLinkToUrl(s.getPermalink), s.getTitle, s.getSelftext, Other("Not implemented at the moment"))
    logger.debug(s"Challenge Parsed: $challenge")

    val commentNode = s.getComments
    for (c <- commentNode.walkTree()) {

    }
  }

  def permaLinkToUrl(relativePath: String): URL = new URL("reddit.com/" + relativePath)
}

case class User(id: String, username: String)
case class Challenge(id: String, created: Date, link: URL, title: String, body: String, difficulty: Difficulty)
case class Response(id: String, created: Date, link: URL, body: String, karma: Int)
case class ChallengeResponse(id: String, challenge: Challenge, user: User, responses: Seq[Response])

sealed trait Difficulty
case object Easy extends Difficulty
case object Intermediate extends Difficulty
case object Hard extends Difficulty
case class Other(tag: String) extends Difficulty
