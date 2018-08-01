package prelude

import scala.util.matching.Regex

package object extension extends ExtensionImports

trait ExtensionImports {

  implicit def toRichRegex(repr: Regex): RichRegex = new RichRegex(repr)
}

final class RichRegex(val repr: Regex) extends AnyVal {
  def exactMatch(str: String): Boolean = str.matches(repr.regex)
}
