import akka.dispatch.Future
import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.{HttpRequest, HttpResponse, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}

trait CalculatorService extends BlueEyesServiceBuilder with BijectionsChunkString {
  val calculatorService = service("calculatorService", "1.0.0") {
    context =>
      startup {
        Future { () }
      } ->
      request { config: Unit =>
        path("/add" / 'number1 / 'number2) {
          (request: HttpRequest[ByteChunk]) =>
            try {
              val sum =
                for {
                  number1 <- request.parameters.get('number1).map(_.toInt)
                  number2 <- request.parameters.get('number2).map(_.toInt)
                } yield (number1 + number2).toString

              Future {
                HttpResponse[ByteChunk](content = Some(sum.toString))
              }
            } catch {
                case e: NumberFormatException =>
                  Future {
                    HttpResponse[ByteChunk](status = HttpStatus(BadRequest))
                  }
            }
        } ~
        path("/multiply" / 'number1 / 'number2) {
          (request: HttpRequest[ByteChunk]) =>
            try {
              val product =
                for {
                  number1 <- request.parameters.get('number1).map(_.toInt)
                  number2 <- request.parameters.get('number2).map(_.toInt)
                } yield (number1 * number2).toString

              Future {
                HttpResponse[ByteChunk](content = Some(product.toString))
              }
            } catch {
                case e: NumberFormatException =>
                  Future {
                    HttpResponse[ByteChunk](status = HttpStatus(BadRequest))
                  }
            }
        }
      } ->
      shutdown { config =>
        println("Shutting down")
        Future { () }
      }
  }
}
