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
              val number1 = request.parameters('number1).toInt
              val number2 = request.parameters('number2).toInt
              val sum = (number1 + number2).toString

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
              val number1 = request.parameters('number1).toInt
              val number2 = request.parameters('number2).toInt
              val product = (number1 * number2).toString

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
