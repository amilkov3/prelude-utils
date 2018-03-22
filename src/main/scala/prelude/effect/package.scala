package prelude

package object effect extends EffectImports

trait EffectImports extends {}
  with cats.effect.LiftIO.ToLiftIOOps
  with cats.effect.Async.ToAsyncOps
  with cats.effect.Sync.ToSyncOps
  with cats.effect.Effect.ToEffectOps
  with cats.effect.Concurrent.ToConcurrentOps
  with cats.effect.ConcurrentEffect.ToConcurrentEffectOps
  with effect.IdInstances
{

  /** Types */
  type IO[+A]= cats.effect.IO[A]
  val IO = cats.effect.IO

  /** Effect typeclasses */
  type Sync[F[_]] = cats.effect.Sync[F]
  val Sync = cats.effect.Sync

  type Async[F[_]] = cats.effect.Async[F]
  val Async = cats.effect.Async

  type Concurrent[F[_]] = cats.effect.Concurrent[F]
  val Concurrent = cats.effect.Concurrent

  type Effect[F[_]] = cats.effect.Effect[F]
  val Effect = cats.effect.Effect

  type ConcurrentEffect[F[_]] = cats.effect.ConcurrentEffect[F]
  val ConcurrentEffect = cats.effect.ConcurrentEffect

  /** For handling effect errors */
  type ApplicativeError[F[_], E] = cats.ApplicativeError[F, E]
  val ApplicativeError = cats.ApplicativeError

  type MonadError[F[_], E] = cats.MonadError[F, E]
  val MonadError = cats.MonadError
}
