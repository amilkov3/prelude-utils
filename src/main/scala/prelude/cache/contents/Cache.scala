package prelude.cache.contents

import prelude.effect._
import prelude.error._

/** Typeclass for serializing a cache key or value `In` to
  * the underlying cache's key type `Out`  */
trait SerializableKV[In] {
  type Out
  def serialize(k: In): Out
}

object SerializableKV {
  /** The `Aux` pattern creates a dependent type association between
    * , in this case, an A and a B */
  type Aux[A, B] = SerializableKV[A] { type Out = B }

  def apply[A, B](implicit ev: Aux[A, B]): Aux[A, B] = ev

  def instance[A, B](f: A => B): Aux[A, B] = new SerializableKV[A] {
    type Out = B
    override def serialize(k: A): B = f(k)
  }
}

/** Typeclass for deserialzing the underlying cache
  * value representation `Out` to a type `In` */
trait DeserializableV[Out] {
  type In
  def deserialize(k: Out): Either[AppFailure, In]
}

object DeserializableV {
  type Aux[A, B] = DeserializableV[A] { type In = B }

  def apply[A, B](implicit ev: Aux[A, B]): Aux[A, B] = ev

  def instance[A, B](f: A => Either[AppFailure, B]): Aux[A, B] = new DeserializableV[A] {
    type In = B
    override def deserialize(k: A): Either[AppFailure, B] = f(k)
  }
}

/** Typeclass used simply to create a compilation constraint ensuring
  * that a key value pair `K` `V` may be cacheable */
trait CacheableKVPair[K] {
  type V
}

object CacheableKVPair {
  type Aux[K, VV] = CacheableKVPair[K] { type V = VV }

  def apply[K, V](implicit ev: Aux[K, V]): Aux[K, V] = ev

  def instance[K, VV]: Aux[K, VV] = new CacheableKVPair[K] {
    type V = VV
  }
}

/** Generic cache client */
trait EfCacheClient[F[_], KK, VV] {
  def get[K, V](k: K)(implicit
    ev1: CacheableKVPair.Aux[K, V],
    ev2: SerializableKV.Aux[K, KK],
    ev3: DeserializableV.Aux[VV, V]
  ): F[Option[Either[AppFailure, V]]]

  def put[K, V](k: K, v: V)(implicit
    ev1: CacheableKVPair.Aux[K, V],
    ev2: SerializableKV.Aux[K, KK],
    ev3: SerializableKV.Aux[V, VV]
  ): F[Unit]
}

/** Effect abstracted base impl. If your underlying client is async and only returns
  * say `Future`s, extend `EfCacheClient` instead */
abstract class EfBaseCacheClient[F[_]: Effect, KK, VV] extends EfCacheClient[F, KK, VV] {

  override final def get[K, V](k: K)(implicit
    ev1: CacheableKVPair.Aux[K, V],
    ev2: SerializableKV.Aux[K, KK],
    ev3: DeserializableV.Aux[VV, V]
  ): F[Option[Either[AppFailure, V]]] = {
    Effect[F].pure(
      get(ev2.serialize(k)).map(ev3.deserialize)
    )
  }

  override final def put[K, V](k: K, v: V)(implicit
    ev1: CacheableKVPair.Aux[K, V],
    ev2: SerializableKV.Aux[K, KK],
    ev3: SerializableKV.Aux[V, VV]
  ): F[Unit] = {
    Effect[F].pure(
      put(ev2.serialize(k), ev3.serialize(v))
    )
  }

  protected def get(k: KK): Option[VV]

  protected def put(k: KK, v: VV): Unit
}
