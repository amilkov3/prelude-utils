package prelude.http
package cached

import prelude.effect._
import prelude.cache._
import prelude.cache.contents.CacheableKVPair
import prelude.json._
import prelude.error._
import prelude.category._
import prelude.logging._

import shapeless.Typeable
import scala.concurrent.ExecutionContext

/** Client that caches GET requests. */
abstract class BaseCachedJsonHttpClient[F[_]: Effect, KK, VV] {

  def get[A: JsonDecodable](
    url: Url,
    headers: Map[String, String] = Map.empty
  )(implicit
    ev1: CacheableKVPair.Aux[Url, A],
    ev2: SerializableKV.Aux[Url, KK],
    ev3: DeserializableV.Aux[A, VV],
    ev4: SerializableKV.Aux[A, VV],
    ev5: Typeable[A],
    ec: ExecutionContext
  ): F[Either[HttpResponse[Either[JsonErr, A]], Either[AppFailure, A]]]

  /*def fullyAbsolvedGet[A: JsonDecodable](
    url: Url,
    headers: Map[String, String] = Map.empty
  )(implicit
    ev1: CacheableKVPair.Aux[Url, A],
    ev2: SerializableKV.Aux[Url, KK],
    ev3: DeserializableV.Aux[A, VV],
    ev4: SerializableKV.Aux[A, VV],
    ev5: Typeable[A]
  ): F[A]*/

}

/** Impl. `client` is exposed if user needs to make other requests */
class CachedJsonHttpClient[F[_]: Effect, KK, VV](
  val client: JsonHttpClient[F],
  cache: BaseCacheClient[F, KK, VV]
) extends BaseCachedJsonHttpClient[F, KK, VV] with StrictLogging {

  /*_*/
  override def get[A : JsonDecodable](
    url: Url,
    headers: Map[String, String] = Map.empty
  )(implicit
    ev1: CacheableKVPair.Aux[Url, A],
    ev2: SerializableKV.Aux[Url, KK],
    ev3: DeserializableV.Aux[A, VV],
    ev4: SerializableKV.Aux[A, VV],
    ev5: Typeable[A],
    ec: ExecutionContext
  ): F[Either[HttpResponse[Either[JsonErr, A]], Either[AppFailure, A]]] = {
    cache.get[Url, A](url).flatMap{ _.cata(
      _.asRight[HttpResponse[Either[JsonErr, A]]].pure[F],
      {
        client.get[A](url).map{r => r.body.map { body =>
          body.foreach{ a =>
            (Async.shift[F](ec) *> cache.put[Url, A](url, a)).runAsync{
              case Left(err) =>
                IO(logger.error(
                  s"Failed to put ${ev5.describe} into cache after GET. Info: ${err.getMessage}"
                ))
              case _ => IO.unit
            }.unsafeRunSync()
          }
          body
        }
        r.asLeft[Either[AppFailure, A]]
      }}
    )}
  }

  /*def fullyAbsolvedGet[A: JsonDecodable](
    url: Url,
    headers: Map[String, String] = Map.empty
  )(implicit
    ev1: CacheableKVPair.Aux[Url, A],
    ev2: SerializableKV.Aux[Url, KK],
    ev3: DeserializableV.Aux[A, VV],
    ev4: SerializableKV.Aux[A, VV],
    ev5: Typeable[A]
  ): F[A]*/
}
