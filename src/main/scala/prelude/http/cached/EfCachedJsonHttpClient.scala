package prelude.http
package cached

import prelude.effect._
import prelude.cache._
import prelude.json._
import prelude.error._
import prelude.category._
import prelude.logging._

import scala.reflect.runtime.universe.TypeTag
import scala.concurrent.ExecutionContext

/** Client that caches GET requests. This trait forces you to use the same
  * cache and http client effect, because ideally you should only have one
  * effectful monad in your service. Also if you consider that putting to an in memory
  * cached back by a mutable map for example is a side effect in and of itself (though
  * not the usual "side-effect" we think about i.e. an i/o event) it might make
  * more sense to wrap said action in `IO` anyway, although consider also that this does not
  * make the action itself RT because you are mutating shared state regardless */
abstract class EfCachedJsonHttpClient[F[_]: Effect, KK, VV] {

  def get[A: JsonDecodable](
    url: Url,
    headers: Map[String, String] = Map.empty
  )(implicit
    ev1: CacheableKVPair.Aux[Url, A],
    ev2: SerializableKV.Aux[Url, KK],
    ev3: DeserializableV.Aux[VV, A],
    ev4: SerializableKV.Aux[A, VV],
    ev5: TypeTag[A],
    ec: ExecutionContext
  ): F[Either[HttpResponse[Either[JsonErr, A]], Either[AppFailure, A]]]

}

/** Impl. `client` is exposed if user needs to make other requests */
class CachedJsonHttpClient[F[_]: Effect, KK, VV](
  val client: JsonHttpClient[F],
  cache: EfBaseCacheClient[F, KK, VV]
) extends EfCachedJsonHttpClient[F, KK, VV] with Logging {

  /*_*/
  override def get[A : JsonDecodable](
    url: Url,
    headers: Map[String, String] = Map.empty
  )(implicit
    ev1: CacheableKVPair.Aux[Url, A],
    ev2: SerializableKV.Aux[Url, KK],
    ev3: DeserializableV.Aux[VV, A],
    ev4: SerializableKV.Aux[A, VV],
    ev5: TypeTag[A],
    ec: ExecutionContext
  ): F[Either[HttpResponse[Either[JsonErr, A]], Either[AppFailure, A]]] = {
    cache.get[Url, A](url).flatMap{ _.cata(
      _.asRight[HttpResponse[Either[JsonErr, A]]].pure[F],
      {
        client.get[A](url).map{r => r.body.map { body =>
          body.foreach{ a =>
            cache.put[Url, A](url, a).unsafeForkAsync({
              case Left(err) =>
                logger.error(s"Failed to put ${ev5.tpe} into cache after GET. Info: ${err.getMessage}")
              case _ => ()
            })
          }
          body
        }
        r.asLeft[Either[AppFailure, A]]
      }}
    )}
  }
}
