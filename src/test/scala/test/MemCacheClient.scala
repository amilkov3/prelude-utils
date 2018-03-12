package test

import prelude.cache.contents.EfBaseCacheClient
import prelude.effect._

final class MemCacheClient[F[_]: Effect, KK, VV] extends EfBaseCacheClient[F, KK, VV] {

  val cache  = scala.collection.mutable.Map.empty[KK, VV]

  override protected def get(k: KK): Option[VV] = cache.get(k)

  override protected def put(k: KK, v: VV): Unit = cache.put(k, v)

}
