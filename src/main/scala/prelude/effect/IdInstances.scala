/*package prelude.effect

import cats.Id
import cats.effect.ExitCase

trait IdInstances {

  /** Just used for compilation */
  trait Id[A]

  implicit val effectId: Effect[Id] =  new Effect[Id] {
    override def runSyncStep[A](fa: Id[A]): IO[Either[Id[A], A]] = IO(Right(fa))
    override def asyncF[A](k: (Either[Throwable, A] => Unit) => Id[Unit]): Id[A] = Id.asInstanceOf[A]
    override def bracketCase[A, B](acquire: Id[A])(use: A => Id[B])(release: (A, ExitCase[Throwable]) => Id[Unit]): Id[B] = use(acquire)
    override def runAsync[A](fa: Id[A])(cb: (Either[Throwable, A]) => IO[Unit]): IO[Unit] = IO(())
    override def async[A](k: ((Either[Throwable, A]) => Unit) => Unit): Id[A] = Id.asInstanceOf[A]
    override def suspend[A](thunk: => Id[A]): Id[A] = thunk
    override def raiseError[A](e: Throwable): Id[A] = Id.asInstanceOf[A]
    override def handleErrorWith[A](fa: Id[A])(f: (Throwable) => Id[A]): Id[A] = fa
    override def pure[A](x: A): Id[A] = x
    override def tailRecM[A, B](a: A)(f: (A) => Id[Either[A, B]]): Id[B] = Id.asInstanceOf[B]
    override def flatMap[A, B](fa: Id[A])(f: (A) => Id[B]): Id[B] = Id.asInstanceOf[B]
  }
}*/
