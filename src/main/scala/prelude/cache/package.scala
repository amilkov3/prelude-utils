package prelude

package object cache extends CacheImports

trait CacheImports {

  type SerializableKV[A] = cache.contents.SerializableKV[A]
  val SerializableKV = cache.contents.SerializableKV

  type DeserializableV[A] = cache.contents.DeserializableV[A]
  val DeserializableV = cache.contents.DeserializableV

  type CacheableKVPair[V] = cache.contents.CacheableKVPair[V]
  val CacheableKVPair = cache.contents.CacheableKVPair

  type CacheClient[F[_], K, V] = cache.contents.CacheClient[F, K, V]

  type BaseCacheClient[F[_], K, V] = cache.contents.BaseCacheClient[F, K, V]

}
