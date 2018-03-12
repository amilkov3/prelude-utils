package prelude.cache

import cats.Id
import test._
import prelude.effect._
import prelude.category._
import prelude.error._
import scodec._
import scodec.bits.BitVector
import scodec.codecs.implicits._

class EfCacheClientUnitTest extends UnitPropertySpec {

  case class Foo(x: String, y: Int)
  case class Bar(a: Double, b: Boolean)

  implicit val serializableKVFoo: SerializableKV.Aux[Foo, String] =
    SerializableKV.instance[Foo, String](foo => s"${foo.x},${foo.y}")

  implicit val serializableKVBar: SerializableKV.Aux[Bar, Array[Byte]] =
    SerializableKV.instance[Bar, Array[Byte]](
      Codec[Bar].encode(_).toEither.asRight.toByteArray
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

  property("should put and get, serializing and deserializing correctly") {
    val foo = Foo("hello", 2)
    cacheClient.put[Foo, Bar](foo, Bar(1.5d, true))
    cacheClient.get[Foo, Bar](foo).asSome.asRight
  }

}
