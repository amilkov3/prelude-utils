package prelude.http

import com.softwaremill.sttp._
import prelude.effect._
import prelude.category._

trait HttpImports extends {}
  with CacheClientImports
  with HttpClientImports
  with SttpBackendImports
{

  type Url = url.Url
  val Url = url.Url

  type Host = url.Host
  val Host = url.Host

  type Path = url.Path
  val Path = url.Path

  type HttpResponse[A] = Response[A]
  object HttpResponse {
    def empty200[A](a: A) = Response(a.asRight[String], 200, "OK", List.empty, List.empty)
  }
}

trait CacheClientImports {

  type EfCachedJsonHttpClient[F[_], K, V] = cached.EfCachedJsonHttpClient[F, K, V]

  type CachedJsonHttpClient[F[_], K, V] = cached.CachedJsonHttpClient[F, K, V]
}

trait HttpClientImports {

  type HttpClientConf = client.HttpClientConf

  type EfJsonHttpClient[F[_]] = client.EfJsonHttpClient[F]

  type JsonHttpClient[F[_]] = client.JsonHttpClient[F]
}

trait SttpBackendImports {

  type SttpBackend[R[_], -S] = com.softwaremill.sttp.SttpBackend[R, S]

  /** Simple IO wrapper around  [[java.net.HttpURLConnection]] */
  class IOHttpURLConnectionBackend extends SttpBackend[IO, Nothing] {

    val client = HttpURLConnectionBackend()

    override def send[T](request: Request[T, Nothing]): IO[Response[T]] =
      IO(client.send(request))

    override def close(): Unit = ()

    /** Translates `cats.MonadError` to `com.softwaremill.sttp.MonadError[IO]` */
    override def responseMonad: com.softwaremill.sttp.MonadError[IO] = new com.softwaremill.sttp.MonadError[IO] {
      override def error[T](t: Throwable): IO[T] = IO.raiseError(t)
      override def flatMap[T, T2](fa: IO[T])(f: T => IO[T2]): IO[T2] = fa.flatMap(f)
      override def unit[T](t: T): IO[T] = IO(t)
      override def map[T, T2](fa: IO[T])(f: T => T2): IO[T2] = fa.map(f)
      override def handleWrappedError[T](rt: IO[T])(h: PartialFunction[Throwable, IO[T]]): IO[T] = rt.recoverWith(h)
    }
  }
}

