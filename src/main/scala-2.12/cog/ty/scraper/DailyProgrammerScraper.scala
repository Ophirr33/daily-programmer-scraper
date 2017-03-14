package cog.ty.scraper

import java.io.File
import java.net.URL

import com.typesafe.scalalogging.Logger
import net.dean.jraw.RedditClient
import net.dean.jraw.http.oauth.Credentials
import net.dean.jraw.http.{NetworkException, UserAgent}
import net.dean.jraw.models.{CommentNode, Listing, Submission}
import net.dean.jraw.paginators.{Sorting, SubredditPaginator, TimePeriod}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
  * Scrapes every submission from the daily programmer subreddit using JRAW
  * Takes in a client and a logger just in case I want to set up tests in
  * the future using a mocking class of some sort.
  * @author ophirr33
  */
case class DailyProgrammerScraper(redditClient: RedditClient, logger: Logger) {
  def scrapeFromClient: (Seq[Challenge], Seq[User], Seq[Response]) = {
    val dailyProgrammer =
      new SubredditPaginator(redditClient, "dailyprogrammer")
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
            logger.debug(
              s"Successfully matched regex for `${s.getTitle}` and id `${s.getId}`")
            Try(redditClient.getSubmission(s.getId)) match {
              case Success(submissionWithComments) =>
                logger.debug(
                  s"Successfully loaded submission with id `${s.getId}`")
                parseSubmission(submissionWithComments, Difficulty(difficulty)).foreach {
                  case (challenge, user, response) =>
                    challenges.append(challenge)
                    users.append(user)
                    responses.append(response)
                    logger.debug(
                      "Successfully parsed a response with challenge id"
                        + s"`${challenge.id}`, user id `${user.username}`, and response id `${response.id}`")
                }
              case Failure(network: NetworkException) =>
                logger.warn(
                  s"Network exception encountered while loading submission with id `${s.getId}`"
                    + s"with message `${network.getResponse}`")
              case Failure(exception: Throwable) => throw exception
            }
          case _ =>
            logger.debug(
              s"Could not parse submission with title `${s.getTitle}`")
        }
      }
    }
    (challenges, users, responses)
  }

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
    import collection.JavaConverters._
    for {
      (c: CommentNode) <- if (comments != null) comments.walkTree().asScala
      else {
        logger.warn(s"Received null instead of comments for submission `${s.getId}`")
        Iterable.empty[CommentNode]
      }
    } yield {
      val comment = c.getComment
      val user =
        User(comment.getAuthor)
      val response = Response(comment.getId,
                              challenge,
                              user,
                              comment.getCreated,
                              Option(comment.getUrl).map(new URL(_)),
                              comment.getBody)
      (challenge, user, response)
    }
  }

  def permaLinkToUrl(relativePath: String): URL =
    new URL("https://reddit.com/" + relativePath)
}

object DailyProgrammerScraper {

  /**
    * The config file should be on the class path and consist of two lines.
    * The first line should be the user agent, and the second line credentials.
    * e.g.:  desktop cog.ty.scraper v0.1 NiceGuy_Ty
    *        username password clientId clientSecret   * @param configFile
    * @return
    */
   def apply(credentialsFileName: String,
            version: String,
            user: String): DailyProgrammerScraper = {
    val logger = Logger[DailyProgrammerScraper]
    val credentialsFile = new File(
      getClass.getClassLoader.getResource(credentialsFileName).getFile)
    val credentials =
      Source.fromFile(credentialsFile).getLines().next().split(" ")
    val userAgent =
      UserAgent.of("desktop", getClass.getPackage.getName, version, user)
    logger.debug(s"Using User Agent `${userAgent.toString}`")

    val scriptCredentials = Credentials.script(credentials(0),
                                               credentials(1),
                                               credentials(2),
                                               credentials(3))
    val redditClient = new RedditClient(userAgent)
    val authData = redditClient.getOAuthHelper.easyAuth(scriptCredentials)
    redditClient.authenticate(authData)

    DailyProgrammerScraper(redditClient, logger)
  }
}
