package demo.api.catalogue

import com.lightbend.lagom.scaladsl.api.deser.DefaultExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}
import play.api.Environment

import scala.util.control.NoStackTrace


trait ExtraTransportExceptions {
  case class BadRequest(message: ExceptionMessage) extends TransportException(TransportErrorCode.BadRequest, message) with NoStackTrace
  object BadRequest {
    def apply(message: String): BadRequest = apply(new ExceptionMessage(classOf[BadRequest].getSimpleName, message))
  }
}

object ExtraTransportExceptions extends ExtraTransportExceptions {

  final class CustomExceptionSerializer(environment: Environment) extends DefaultExceptionSerializer(environment) {
    protected override def fromCodeAndMessage(transportErrorCode: TransportErrorCode, exceptionMessage: ExceptionMessage): Throwable = {
      transportErrorCode match {
        case TransportErrorCode.BadRequest ⇒ BadRequest(exceptionMessage)
        case _                             ⇒ super.fromCodeAndMessage(transportErrorCode, exceptionMessage)
      }
    }
  }
}