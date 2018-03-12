package prelude

package object error extends ErrorImports

trait ErrorImports {

  type AppException = prelude.error.content.AppException
  val AppException = prelude.error.content.AppException

  type AppFailure = prelude.error.content.AppFailure

  type UpstreamFailure = prelude.error.content.UpstreamFailure
  val UpstreamFailure = prelude.error.content.UpstreamFailure

  type UserFailure = prelude.error.content.UserFailure
  val UserFailure = prelude.error.content.UserFailure

  type InternalFailure = prelude.error.content.InternalFailure
  val InternalFailure = prelude.error.content.InternalFailure

  type UserComponent = prelude.error.content.UserComponent
  type InternalComponent = prelude.error.content.InternalComponent
  type UpstreamComponent = prelude.error.content.UpstreamComponent

  /** User components */
  type JsonErr = prelude.error.content.JsonErr
  type JsonParseErr = prelude.error.content.JsonParseErr
  val JsonParseErr = prelude.error.content.JsonParseErr
  type JsonDecodeErr = prelude.error.content.JsonDecodeErr
  val JsonDecodeErr = prelude.error.content.JsonDecodeErr

  /** Service failures */
  type ServiceFailure = prelude.error.content.ServiceFailure

  type ServiceHttpError = prelude.error.content.ServiceHttpError
  val ServiceHttpError = prelude.error.content.ServiceHttpError

  type ServiceInvalidPayload = prelude.error.content.ServiceInvalidPayload
  val ServiceInvalidPayload = prelude.error.content.ServiceInvalidPayload

  type ServiceUnreachable = prelude.error.content.ServiceUnreachable
  val ServiceUnreachable = prelude.error.content.ServiceUnreachable

}
