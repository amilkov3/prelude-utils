package prelude

import prelude.category._
import scala.util.matching.Regex

package object extension extends ExtensionImports

trait ExtensionImports {

  implicit def toRichRegex(repr: Regex): RichRegex = new RichRegex(repr)
}

final class RichRegex(val repr: Regex) extends AnyVal {
  def exactMatch(str: String): Boolean = {
    repr.findFirstIn(str).cata(_.length == str.length, false)
  }
}
