package prelude

package object effect extends EffectImports

trait EffectImports extends {}
  with cats.effect.LiftIO.ToLiftIOOps
  with cats.effect.Async.ToAsyncOps
  with cats.effect.Sync.ToSyncOps
  with cats.effect.Effect.ToEffectOps
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

  type Effect[F[_]] = cats.effect.Effect[F]
  val Effect = cats.effect.Effect

  type ApplicativeError[F[_], E] = cats.ApplicativeError[F, E]
  val ApplicativeError = cats.ApplicativeError

  type MonadError[F[_], E] = cats.MonadError[F, E]
  val MonadError = cats.MonadError

  implicit def toAsyncOps[F[_]: Effect, A](repr: F[A]): effect.AsyncOps[F, A] =
    new effect.AsyncOps[F, A](repr)

  // TODO: Compiler fails to lift `F[Unit]` via `toAsyncOps`
  implicit def toAsyncOps1[F[_]: Effect](repr: F[Unit]): effect.AsyncOps[F, Unit] =
    new effect.AsyncOps[F, Unit](repr)

}
