package prelude.http

import cats.Show
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._
import prelude.category._

object url {

  case class Url(
    host: Host,
    path: Path,
    isHttps: Boolean = true,
    port: Option[Int] = None,
    params: Map[String, String] = Map.empty,
    fragment: Option[String] = None
  )

  object Url {
    implicit val showUrl: cats.Show[Url] = new Show[Url] {
      override def show(t: Url): String = {
        val paramStr = t.params.isEmpty.fold(
          "",
          t.params.foldRight("?") { case ((k, v), str) => s"$str$k=$v&" }.dropRight(1)
        )
        s"${t.isHttps.fold("https", "http")}://${t.host.repr}${t.port.cata(p => s":$p", "")}/${t.path.build}${paramStr}${t.fragment.cata(f => s"#$f", "")}"
      }
    }
  }

  @newtype case class Host(repr: String)
  object Host {

    /** https://www.regextester.com/23 */
    private val hostR =
      """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""".r

    def create(hostStr: String): Either[Throwable, Host] = {
      Either.catchNonFatal(unsafeCreate(hostStr))
    }

    def unsafeCreate(hostStr: String): Host = {
      hostR.findFirstIn(hostStr).cata(
        Host(_),
        throw new IllegalArgumentException(s"Invalid hostname: $hostStr")
      )
    }
  }

  @newtype case class Path(repr: Array[String]) {
    def toList: List[String] = repr.toList

    def build: String = repr.mkString("/")
  }
  object Path {
    def create(pathStr: String): Either[Throwable, Path] = {
      Either.catchNonFatal(unsafeCreate(pathStr))
    }

    def unsafeCreate(pathStr: String): Path = {
      pathStr.startsWith("/").fold(
        Path(pathStr.substring(1).split("/")),
        throw new IllegalArgumentException(s"Invalid uri $pathStr")
      )
    }
  }

}
