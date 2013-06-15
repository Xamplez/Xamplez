package models

import scala.util.parsing.combinator.RegexParsers

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import play.api.Play.current

case class Tag(name: String) extends AnyVal

object Tag {
  implicit val tagReads = Json.reads[Tag]
  implicit val tagWrites = Json.writes[Tag]

  val hash = "#"

  def create(name: String) = Tag(name.toLowerCase)

  object TagParser extends RegexParsers {
    //override val skipWhitespace = false

    def hash: Parser[String] = Tag.hash
    def hashname: Parser[String] = "[a-zA-Z0-9._]+[^.#\\s]".r
    def hashtag: Parser[Tag] = hash ~> hashname ^^ { t => Tag.create(t) }
    def escapedHash: Parser[String] = hash ~ hash ^^ { _ => Tag.hash }
    def content: Parser[String] = (s"[^${Tag.hash}]").r
    def string: Parser[Seq[Either[String, Tag]]] = (escapedHash ^^ { Left(_) } | hashtag ^^ { Right(_) } | content ^^ { Left(_) } )*

    def apply(input: String): Seq[Either[String, Tag]] = parseAll(string, input) match {
      case Success(result, _) => result
      case failure : NoSuccess => scala.sys.error(failure.msg)
    }

  }

  def fetchTags(str: String): Seq[Tag] = TagParser(str).collect{ case Right(t) => t }
}
