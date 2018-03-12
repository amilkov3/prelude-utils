package prelude.effect

import prelude.category._

import scala.concurrent.ExecutionContext

/** As `cats.effect.IO` provides no concurrency primitives, these
  * extension methods offer us such primitives, though basic for now */
final class AsyncOps[F[_], A](val repr: F[A]) extends AnyVal {

  /**
    * Creates an effect F[A] that will run A in a separate thread,
    * registering a callback upon its completion
    *  */
  def fork(a: => A)(implicit
    ec: ExecutionContext,
    ev: cats.effect.Effect[F]
  ): F[A] = {
    ev.async[A]{ cb =>
      ec.execute(
        new Runnable {
          override def run(): Unit = cb(Either.catchNonFatal(a))
        }
      )
    }
  }

  /** Runs an F[A] in a separate thread, invoking a callback on completion
    * that also runs on that separate thread.
    *
    * Although this particular implementation will catch exceptions thrown
    * in F[A], and pass them to the callback in the form of an Either, it's "unsafe"
    * due to the fact that it is not referentially transparent and performs
    * side effects
    * */
  def unsafeForkAsync(cb: Either[Throwable, A] => Unit)(implicit
    ec: ExecutionContext,
    ev: cats.effect.Effect[F]
  ): Unit = {
    repr.flatMap(fork(_)).runAsync(either => IO(cb(either))).unsafeRunSync
  }
}
