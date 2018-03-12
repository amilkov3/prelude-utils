import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.PropertyChecks

package object test {

  abstract class UnitPropertySpec extends {}
    with PropSpec
    with PropertyChecks
    with Matchers

  implicit def toAssertEitherOps[L, R](repr: Either[L, R]) = new AssertEitherOps(repr)

  implicit def toAssertOptionOps[A](repr: Option[A]) = new AssertOptionOps(repr)

}
