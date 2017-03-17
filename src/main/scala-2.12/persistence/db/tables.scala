import cog.ty.scraper.{Challenge, Response, User}
import slick.jdbc.JdbcProfile
import slick.lifted.{ProvenShape, Tag}
import slick.model.Table

class Tables(driver: JdbcProfile) {
  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def username = column[String]("USERNAME", O.PrimaryKey)
    override def * : ProvenShape[User] = username <> (User.apply, User.unapply)
  }

  class Challenges(tag: Tag) extends Table[Challenge](tag, "CHALLENGES") {
    override def * : ProvenShape[Challenge] = ???
  }

  class Responses(tag: Tag) extends Table[Response](tag, "RESPONSES") {
    override def * : ProvenShape[Response] = ???
  }

}