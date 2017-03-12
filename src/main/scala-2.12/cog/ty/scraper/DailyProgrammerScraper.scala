package cog.ty.scraper

import net.dean.jraw.RedditClient
import net.dean.jraw.http.UserAgent

/**
  * Created by ty on 3/12/17.
  */
class DailyProgrammerScraper {

}

object Demo extends App {
  val userAgent = UserAgent.of("desktop", "cog.ty", "v0.1", "NiceGuy_Ty")
  val redditClient = new RedditClient(userAgent)
}
