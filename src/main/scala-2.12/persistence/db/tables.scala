import cog.ty.scraper.{Challenge, Response, User}
import slick.jdbc.JdbcProfile
import slick.lifted.{ProvenShape, Tag}
import slick.model.Table
import java.util.Date
import java.net.URL

class DAO(driver: JdbcProfile) {
  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def username = column[String]("USERNAME", O.PrimaryKey)
    override def * : ProvenShape[User] = username <> (User.apply, User.unapply)
  }
  val users = TableQuery[Users]

  class Challenges(tag: Tag) extends Table[Challenge](tag, "CHALLENGES") {
    def id = column[String]("ID", O.PrimaryKey)
    def created = column[Date]("CREATED")
    def link = column[String]("LINK")
    def title = column[String]("TITLE")
    def body = column[String]("BODY")
    def difficulty = column[String]("DIFFICULTY")
    override def * : ProvenShape[Challenge] = (id, created, link, title, body, difficulty) <> (
      (row: (String, Date, Option[String], String, String, String)) =>
          Challenge(row._1, row._2, new URL(row._3), row._4, row._5, Difficulty(row._6)),
      (ch: Challenge) =>
          Some(ch.id, ch.created, ch.link.toString, ch.title, ch.body, ch.difficulty.difficulty))
  }
  val challenges = TableQuery[Challenges]

  class Responses(tag: Tag) extends Table[Response](tag, "RESPONSES") {
    def id = column[String]("ID", O.PrimaryKey)
    def challengeID = column[String]("CHALLENGE_ID")
    def username = column[STRING]("USERNAME")
    def created = column[Date]("CREATED")
    def link = column[Option[URL]]("LINK")
    def body = column[String]("BODY")
    def challenge = foreignKey("CHALLENGE_ID", challengeID, challenges)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def user = foreignKey("USERNAME", username, users)(_.username, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    override def * : ProvenShape[Response] = (id, challengeID, username, created, link, body)
  }

  object responses extends TableQuery(new Responses(_)) {
    def insertRow(id: Rep[String], chId: Rep[String], uname: Rep[String], created: Rep[Date], link: Rep[Option[String]], body: Rep[String]) = {
      this += (id, chId, uname, created, link, body)
    }
    val insertCompiled = Compiled(insertRow _)

   def insertResponse(r: Response) = insertCompiled(r.id, r.challenge.id, r.user.username, r.created, r.link.map(_.toString), r.difficulty.difficulty)
  }
}
