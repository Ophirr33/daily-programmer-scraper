// package persistence
// import java.io.{File, PrintWriter}
// import java.net.URL
// import java.util.Date

// import cog.ty.scraper.{Challenge, Difficulty, Response, User}

// /**
//   * @author ty
//   * @since 3/14/17
//   */
// case class CsvPersister(challengeFile: String,
//                         userFile: String,
//                         responseFile: String)
//     extends Persister[Unit] {
//   override def persist(challenge: Seq[Challenge],
//                        users: Seq[User],
//                        responses: Seq[Response]): Unit = {
//     val chw = new PrintWriter(new File(challengeFile))
//     val usw = new PrintWriter(new File(userFile))
//     val rew = new PrintWriter(new File(responseFile))

//     chw.write(challenge.map(ch => deParentheses(ch.toString)).mkString(","))
//     usw.write(challenge.map(us => deParentheses(us.toString)).mkString(","))
//     rew.write(challenge.map(re => deParentheses(re.toString)).mkString(","))

//     chw.close()
//     usw.close()
//     rew.close()
//   }

//   def escape(raw: String): String = {
//     import scala.reflect.runtime.universe._
//     "\"" + Literal(Constant(raw)).toString + "\""
//   }

//   def deParentheses(str: String): String =
//     "\"" + escape(str.substring(1, str.length - 1)) + "\""
// }
// // Todo actually do this. Look into product iterator and just
// // turning an iter of any into what we need
// object Test extends App {
//   val csv = CsvPersister("", "", "")
//   println(
//     csv.deParentheses(
//       Challenge("0981-skd`",
//                 new Date(),
//                 new URL("http://google.com"),
//                 "A very, long, title that \"sucks\"",
//                 "foo",
//                 Difficulty("easy")).toString))
// }
