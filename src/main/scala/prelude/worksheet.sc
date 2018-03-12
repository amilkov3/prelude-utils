

import cats._

//import prelude.effect._



/*trait Foo
case object Bar extends Foo
case object Baz extends Foo

implicit val showFoo: Show[Foo] = new Show[Foo] {
  override def show(t: Foo): String = t.toString
}

def test(a: Foo)(implicit ev: Show[Foo]) = ev.show(a)

test(Bar)*/

object Foo {
  implicit final class Bar(val repr: String) extends AnyVal
}
