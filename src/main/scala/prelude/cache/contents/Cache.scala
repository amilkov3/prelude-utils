package prelude.cache.contents

import prelude.effect._
import prelude.error._

/** Typeclass for serializing a type `A` to
  * some serialized type `Repr`  */
trait SerializableKV[A] {
  type Repr
  def serialize(k: A): Repr
}

object SerializableKV {
  /** The `Aux` pattern creates a dependent type association between
    * , in this case, an A and a B */
  type Aux[A, B] = SerializableKV[A] { type Repr = B }

  def apply[A](implicit ev: SerializableKV[A]): Aux[A, ev.Repr] = ev

  def instance[A, B](f: A => B): Aux[A, B] = new SerializableKV[A] {
    type Repr = B
    override def serialize(k: A): B = f(k)
  }
}

/** Typeclass for deserializing the underlying representation `Repr`
  * to its original type `A` */
trait DeserializableV[A] {
  type Repr
  def deserialize(k: Repr): Either[AppFailure, A]
}

object DeserializableV {
  type Aux[A, B] = DeserializableV[A] { type Repr = B }

  def apply[A](implicit ev: DeserializableV[A]): Aux[A, ev.Repr] = ev

  def instance[A, B](f: B => Either[AppFailure, A]): Aux[A, B] = new DeserializableV[A] {
    type Repr = B
    override def deserialize(k: B): Either[AppFailure, A] = f(k)
  }
}

/** Convenience serialization and deserialization typeclass for a type `A` */
trait CodecV[A] extends SerializableKV[A] with DeserializableV[A]

object CodecV {
  type Aux[A, B] = CodecV[A] { type Repr = B }

  def apply[A](implicit ev: CodecV[A]): Aux[A, ev.Repr]  = ev

  def instance[A, B](
    enc: A => B,
    dec: B => Either[AppFailure, A],
  ): Aux[A, B] = new CodecV[A] {
    type Repr = B
    override def serialize(k: A): B = enc(k)
    override def deserialize(k: B): Either[AppFailure, A] = dec(k)
  }
}

//TODO: Is this really necessary
/** Typeclass used simply to create a compilation constraint ensuring
  * that a key value pair `K` `V` may be cacheable and
  * that a given `V` may only be cached via one key as we already have
  * a similar constraint that a key and value may only have one representation `Repr`
  * via `SerializableKV` and `DeserializableV`. Debating whether there is merit in this */
trait CacheableKVPair[V]{
  type K
}

object CacheableKVPair {

  type Aux[A, B] = CacheableKVPair[B] {type K = A}

  def apply[V](implicit ev: CacheableKVPair[V]): Aux[ev.K, V] = ev

  def instance[A, B]: Aux[A, B] = new CacheableKVPair[B] {
    type K = A
  }
}

/** Generic cache client */
trait CacheClient[F[_], KK, VV] {
  def get[K, V](k: K)(implicit
    ev1: CacheableKVPair.Aux[K, V],
    ev2: SerializableKV.Aux[K, KK],
    ev3: DeserializableV.Aux[V, VV]
  ): F[Option[Either[AppFailure, V]]]

  def put[K, V](k: K, v: V)(implicit
    ev1: CacheableKVPair.Aux[K, V],
    ev2: SerializableKV.Aux[K, KK],
    ev3: SerializableKV.Aux[V, VV]
  ): F[Unit]
}

/** Effect abstracted base impl. If your underlying client is async and only returns
  * say `Future`s, extend `EfCacheClient` instead */
abstract class BaseCacheClient[F[_]: Effect, KK, VV] extends CacheClient[F, KK, VV] {

  override final def get[K, V](k: K)(implicit
    ev1: CacheableKVPair.Aux[K, V],
    ev2: SerializableKV.Aux[K, KK],
    ev3: DeserializableV.Aux[V, VV]
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
