package prelude.http.client

import java.nio.charset.StandardCharsets

import com.softwaremill.sttp
import com.softwaremill.sttp.{Uri => SUrl}
import prelude.category._
import prelude.effect._
import prelude.error._
import prelude.http._
import prelude.json._

import scala.concurrent.duration.FiniteDuration

/** Effect abstracted json http client */
abstract class EfJsonHttpClient[F[_]: Effect] {

  def get[A: JsonDecodable](
    url: Url,
    headers: Map[String, String] = Map.empty
  ): F[HttpResponse[Either[JsonErr, A]]]

  def post[A: JsonEncodable](
    url: Url,
    body: A,
    headers: Map[String, String] = Map.empty
  ): F[HttpResponse[Unit]]
}

/** Wraps `sttp`  */
class JsonHttpClient[F[_]: Effect](
  conf: HttpClientConf
)(implicit ev: SttpBackend[F, Nothing]) extends EfJsonHttpClient[F]{

  private val client = sttp.sttp.readTimeout(conf.readTimeout)

  //TODO:
  /** Handle read time out exception (which should map to [[ServiceUnreachable]]
    * otherwise should raise [[InternalFailure]]) here or in the implicit instance?
    * */
  override def get[A: JsonDecodable](
    url: Url,
    headers: Map[String, String] = Map.empty
  ): F[HttpResponse[Either[JsonErr, A]]] = {
    client
      .get(SUrl(
        url.isHttps.fold("https", "http"),
        None,
        url.host.repr,
        None,
        url.path.toList,
        List.empty[SUrl.QueryFragment],
        None
      ))
      .headers(headers)
      .response(sttp.asString.map{s =>
        for {
          json <- Json.parse(s).leftMap(err => JsonParseErr(err.message): JsonErr)
          a <- json.as[A].leftMap(err => JsonDecodeErr(err.message): JsonErr)
        } yield a
      })
      .send[F]()
  }

  /*_*/
  override def post[A: JsonEncodable](
    url: Url,
    body: A,
    headers: Map[String, String]
  ): F[HttpResponse[Unit]] = {
    client
      .post(SUrl(
        url.isHttps.fold("https", "http"),
        None,
        url.host.repr,
        url.port,
        url.path.toList,
        List.empty[SUrl.QueryFragment],
        None
      ))
      .headers(headers)
      .body(body.asJson.spaces2, StandardCharsets.UTF_8.name)
      .send[F]()
      .map(r => r.copy(body = r.body.map(_ => ())))
  }
}

trait HttpClientConf {
  def readTimeout: FiniteDuration
}
