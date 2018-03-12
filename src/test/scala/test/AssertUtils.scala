package test

/** Assert utils */
final class AssertEitherOps[L, R](val underlying: Either[L, R]) extends AnyVal {

  def asRight: R = underlying.right.getOrElse {
    throw new AssertionError(s"Expected Right, got: $underlying")
  }

  def asLeft: L = underlying.left.getOrElse {
    throw new AssertionError(s"Expected Left, got: $underlying")
  }
}

final class AssertOptionOps[A](val underlying: Option[A]) extends AnyVal {
  def asSome: A = underlying.getOrElse(throw new AssertionError("None was not Some"))
}
