package prelude

package object cache extends CacheImports

trait CacheImports {

  val SerializableKV = cache.contents.SerializableKV

  val DeserializableV = cache.contents.DeserializableV

  val CacheableKVPair = cache.contents.CacheableKVPair

  type EfCacheClient[F[_], K, V] = cache.contents.EfCacheClient[F, K, V]

  type EfBaseCacheClient[F[_], K, V] = cache.contents.EfBaseCacheClient[F, K, V]

}
