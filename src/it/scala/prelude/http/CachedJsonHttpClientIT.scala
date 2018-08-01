package prelude.http

import it._
import test._
import prelude.effect._
import io.circe.generic.JsonCodec
import prelude.cache._
import prelude.category._
import prelude.error._
import prelude.http._
import scodec._
import scodec.bits.BitVector
import scodec.codecs.implicits._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class CachedJsonHttpClientIT extends ITPropertySpec {

  @JsonCodec(decodeOnly = true)
  case class Post(
    userId: Int,
    id: Int,
    title: String,
    body: String
  )

  implicit lazy val sttpBackend: SttpBackend[IO, Nothing] =
    new IOHttpURLConnectionBackend

  implicit val serializableKVUrl: SerializableKV.Aux[Url, String] =
    SerializableKV.instance[Url, String](_.show)

  implicit val serializableKVPost: SerializableKV.Aux[Post, Array[Byte]] =
    SerializableKV.instance[Post, Array[Byte]](
      Codec[Post].encode(_).toEither.asRight.toByteArray
    )

  case object DeserializationFailure extends InternalComponent

  implicit val deserializableV: DeserializableV.Aux[Post, Array[Byte]] =
    DeserializableV.instance[Post, Array[Byte]](
      ba => Codec[Post].decode(BitVector(ba))
        .toEither
        .leftMap(err => InternalFailure(err.message, DeserializationFailure))
        .map(_.value)
    )

  implicit val cacheableKVPair: CacheableKVPair.Aux[Url, Post] = CacheableKVPair.instance

  val cache = new MemCacheClient[IO, String, Array[Byte]]

  val client = new BaseCachedJsonHttpClient[IO, String, Array[Byte]](
    new JsonHttpClient(
      new HttpClientConf {
        override val readTimeout: FiniteDuration = 2.seconds
      }
    ),
    cache
  )

  property("should cache response") {
    val url = Url(
      Host.unsafeCreate("jsonplaceholder.typicode.com"),
      Path.unsafeCreate("/posts/1"),
      true
    )
    client.get[Post](url).unsafeRunSync().asLeft.body.asRight.asRight
    /** Sleep because cache put is async */
    Thread.sleep(1000)
    cache.get[Url, Post](url).unsafeRunSync().asSome.asRight
  }
}
