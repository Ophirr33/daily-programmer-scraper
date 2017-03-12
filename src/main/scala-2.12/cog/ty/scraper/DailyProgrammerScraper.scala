package cog.ty.scraper

import com.typesafe.scalalogging
import java.io.File

import com.typesafe.scalalogging.Logger
import net.dean.jraw.RedditClient
import net.dean.jraw.http.UserAgent
import net.dean.jraw.http.oauth.{Credentials, OAuthHelper}
import net.dean.jraw.models.{Listing, Submission}
import net.dean.jraw.paginators.{Sorting, SubredditPaginator, TimePeriod}

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
    val submissions: Listing[Submission] = dailyProgrammer.next()
    for ((s: Submission) <- submissions.iterator().asScala) {
      logger.info(s"\n  Author: ${s.getAuthor}\n  Score: ${s.getScore}\n  Created: ${s.getCreated}\n  ${s.getTitle}")
    }
  }
}
