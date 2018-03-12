package prelude.error.content

/** For raising errors inside our effect monad */
final case class AppException(e: AppFailure) extends Exception(e.message, e.cause.orNull)

/** Top level failure type all concrete UpsFailures inherit from */
trait AppFailure {
  def message: String
  //TODO: Not used in any of the subtypes
  def cause: Option[Throwable]
}
