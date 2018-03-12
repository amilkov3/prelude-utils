package prelude.error.content

/** An internal failure usually represent an exception thrown by some non
  * side-effectful component inside your service, like encryption for example */
//TODO
/** This is as a simpflied type (where for example UserFailure and UpstreamFailure
  * include the error message in the subtype of the actual component). Perhaps
  * for internal failures that are more nuanced it'd be better to just use the
  * same pattern as UserFailure and Upstream Failure */
final case class InternalFailure private(
  desc: String,
  component: InternalComponent,
  cause: Option[Throwable] = None
)  extends  AppFailure {

  override val message: String = s"${component.toString} failed with: $desc"
}

object InternalFailure {
  def apply(message: String, component: InternalComponent) = {
    new InternalFailure(message, component)
  }
}

/** Extend this to add components with the potential to fail i.e.
  *
  * case object Encryption extends InternalComponent
  * case object Decryption extends InternalComponent
  * case object ThreadPoolExhausted extends InternalComponent
  *  */
trait InternalComponent
