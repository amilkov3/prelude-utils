package prelude.error.content

/** Represents an error on behalf of the user, i.e. invalid json or some header
  * or something is missing */
final case class UserFailure private(
  component: UserComponent,
  cause: Option[Throwable] = None
) extends AppFailure {

  override val message: String = s"Invalid user request. Info: ${component.message}"
}

object UserFailure {
  def apply(component: UserComponent) = {
    new UserFailure(component)
  }
  def apply(message1: String) = {
    new UserFailure(new UserComponent {
      override val message: String = message1
    })
  }
}

/** Extend this to indicate the user component that could fail */
trait UserComponent {
  val code: Int = 400
  def message: String
}

trait InvalidPayload {
  def message: String
}

sealed trait JsonErr extends UserComponent with InvalidPayload

case class JsonDecodeErr(desc: String) extends JsonErr {
  override val code: Int = 422
  override val message: String = s"Invalid json entity, failed with: $desc"
}
case class JsonParseErr(desc: String) extends JsonErr {
  override val code: Int = 415
  override val message: String = s"Invalid json, failed with: $desc"
}
