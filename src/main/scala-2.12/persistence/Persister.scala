package persistence

import cog.ty.scraper.{Challenge, Response, User}

/**
  * A trait that allows various ways to persist the resources that
  * are scraped from daily programmer
  * @author ty
  * @since 3/14/17
  */
trait Persister[A] {
  def persist(challenge: Seq[Challenge],
              users: Seq[User],
              responses: Seq[Response]): A
}
