package prelude

import io.circe._
import io.circe.syntax._
import prelude.error._
import prelude.category._

package object json extends JsonImports

trait JsonImports {
  type Json = io.circe.Json
  val Json = io.circe.Json

  type JsonEncodable[A] = io.circe.Encoder[A]
  val JsonEncodable = io.circe.Encoder
  type JsonDecodable[A] = io.circe.Decoder[A]
  val JsonDecodable = io.circe.Decoder

  implicit def toEncoderOps[A: Encoder](repr: A): EncoderOps[A] = new EncoderOps(repr)

  implicit def toJsonObjOps(repr: Json.type): JsonObjOps = new JsonObjOps(repr)

}

final class JsonObjOps(val repr: Json.type) extends AnyVal {
  def parse(s: String): Either[JsonParseErr, Json] = {
    io.circe.parser.parse(s).leftMap(err => JsonParseErr(err.message))
  }
}
