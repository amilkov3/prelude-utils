
# Prelude utilities

_Utilities common to most services_

* Cache
* Http client
* Cached http client
* `cats-core` and `mouse` imports
* `cats-effect` imports along with concurrency primitives
on `cats.effect.IO`
* App error hierarchy 
* Extension methods to existing types
* `circe` imports
* Logging

## Goal

Obviously with something like this there's a tradeoff
between making it feature rich (so the user doesn't
need to write as much boilerplate of their own) 
yet coupling it to specific dependencies and so limiting its 
applicability and making it more abstract and thus
more flexible/widely applicable yet requiring the user
to write more boilerplate to enrich/tailor it to their
specific use case

I've opted for the former, because as of this writing
cats and circe are superior to any other 
libraries in their respective domains and sttp is simply
a wrapper lib around whatever http backend the user
chooses (i.e. my `http` package is thus a thin wrapper
around a wrapper lib)

## Usage

### Cache

```scala
import prelude._
import scodec._
import scodec.bits.BitVector
import scodec.codecs.implicits._

case class Foo(x: String, y: Int)
case class Bar(a: Double, b: Boolean)
 
implicit val serializableKVFoo: SerializableKV.Aux[Foo, String] =
  SerializableKV.instance[Foo, String](foo => s"${foo.x},${foo.y}")
 
implicit val serializableKVBar: SerializableKV.Aux[Bar, Array[Byte]] =
  SerializableKV.instance[Bar, Array[Byte]](
    Codec[Bar].encode(_).toEither.getOrElse(throw new Exception).toByteArray
  )
 
case object DeserializationFailure extends InternalComponent
 
implicit val deserializableV: DeserializableV.Aux[Array[Byte], Bar] =
  DeserializableV.instance[Array[Byte], Bar](
    ba => Codec[Bar].decode(BitVector(ba))
      .toEither
      .leftMap(err => InternalFailure(err.message, DeserializationFailure))
      .map(_.value)
	)

implicit val cacheableKVPair: CacheableKVPair.Aux[Foo, Bar] = CacheableKVPair.instance
 
val cacheClient = new MemCacheClient[Id, String, Array[Byte]]
 
val foo = Foo("hello", 2)
cacheClient.put[Foo, Bar](foo, Bar(1.5d, true))
cacheClient.get[Foo, Bar](foo)
```

### Http client

```scala

import prelude._
import io.circe.generic.JsonCodec

@JsonCodec(decodeOnly = true)
case class Post(
  userId: Int,
  id: Int,
  title: String,
  body: String
)

implicit lazy val sttpBackend: SttpBackend[IO, Nothing] =
  new IOHttpURLConnectionBackend

val client = new JsonHttpClient[IO](new HttpClientConf {
  override val readTimeout: FiniteDuration = 2.seconds
})

val url = Url(
  Host.unsafeCreate("jsonplaceholder.typicode.com"),
  Path.unsafeCreate("/posts/1"),
  true
)

val res: HttpResponse[Either[JsonErr, Post]] = 
  client.get[Post](url).unsafeRunSync()
```

### Cached json http client

```scala

//continued
import scodec._
import scodec.bits.BitVector
import scodec.codecs.implicits._

implicit lazy val sttpBackend: SttpBackend[IO, Nothing] =
  new IOHttpURLConnectionBackend

implicit val serializableKVUrl: SerializableKV.Aux[Url, String] =
  SerializableKV.instance[Url, String](_.show)

implicit val serializableKVPost: SerializableKV.Aux[Post, Array[Byte]] = 
  SerializableKV.instance[Post, Array[Byte]](
    Codec[Post].encode(_).toEither.getOrElse(throw new Exception).toByteArray
  )

case object DeserializationFailure extends InternalComponent

implicit val deserializableV: DeserializableV.Aux[Array[Byte], Post] =
  DeserializableV.instance[Array[Byte], Post](
    ba => Codec[Post].decode(BitVector(ba))
      .toEither
      .leftMap(err => InternalFailure(err.message, DeserializationFailure))
      .map(_.value)
  )

implicit val cacheableKVPair: CacheableKVPair.Aux[Url, Post] = CacheableKVPair.instance

final class MemCacheClient[F[_]: Effect, KK, VV] extends EfBaseCacheClient[F, KK, VV] {

	/** Not thread safe as this is just for demo purposes */
  val cache  = scala.collection.mutable.Map.empty[KK, VV]

  override protected def get(k: KK): Option[VV] = cache.get(k)

  override protected def put(k: KK, v: VV): Unit = cache.put(k, v)

}

val cacheClient = new CachedJsonHttpClient[IO, String, Array[Byte]](
  new JsonHttpClient(
    new HttpClientConf {
      override val readTimeout: FiniteDuration = 2.seconds
    }
  ),
  new MemCacheClient[IO, String, Array[Byte]]
)

val res: Either[HttpResponse[Either[JsonErr, A]], Either[AppFailure, A]] =
  cacheClient.get[Post](uri).unsafeRunSync()
```

## Future work

Obviously we have a dependency on `cats`. When 
Scalaz 8 comes out there'll be `prelude-scalaz`
and `prelude-cats` modules

In addition, there will be modules for specific 
technologies/use cases (The code for these is 
actually already written)
* `prelude-mongo` - a nice functional wrapper around the 
Casbah driver
* `prelude-geo` - functional wrapper around `jgeohash`, a
Java lib
