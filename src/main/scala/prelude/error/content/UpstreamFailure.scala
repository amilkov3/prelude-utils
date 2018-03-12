package prelude.error.content

import prelude.category._
import prelude.http._

/** Represents a failure in some upstream component like a db or service */
final case class UpstreamFailure private (
  component: UpstreamComponent,
  cause: Option[Throwable] = None
) extends AppFailure {

  override val message: String = s"Upstream failure. Info: ${component.message}"
}

object UpstreamFailure {
  def apply(component: UpstreamComponent): UpstreamFailure = {
    new UpstreamFailure(component)
  }
}

/** Extend this add upstream components that may fail, like db ops for example */
trait UpstreamComponent {
  def code: Int
  def message: String
}

trait ServiceFailure extends UpstreamComponent {
  def name: String
  override val code = 503
}

/** Service returned a success, but payload could not be decoded */
case class ServiceInvalidPayload (
  name: String,
  payload: InvalidPayload
) extends ServiceFailure {
  override val message: String = s"Invalid payload from $name. Info: ${payload.message}"
}

/** Service returned an error */
case class ServiceHttpError (
  name: String,
  respCode: Int,
  body: String
) extends ServiceFailure {
  override val message: String = s"$name call returned a $respCode. Body: $body"
}

/** Http client couldn't even reach the service */
case class ServiceUnreachable (
  name: String,
  url: Url,
  ex: Throwable
) extends ServiceFailure {
  override val message: String =
    s"Error while contacting service: $name. URL: ${url.show}. Info: ${ex.getMessage}"
}
