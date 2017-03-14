package cog.ty.scraper

/**
  * @author ty
  * @since 3/14/17
  */
object Main extends App {
  val scraper =
    DailyProgrammerScraper("credentials.conf", "0.1.0", "NiceGuy_Ty")
  val (challenges, users, responses) = scraper.scrapeFromClient
  scraper.logger.info(
    s"LENGTHS: ch: ${challenges.length}, us: ${users.length}, r: ${responses.length}")
}
