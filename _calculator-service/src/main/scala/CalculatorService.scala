import akka.Promise
import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.{HttpRequest, HttpResponse, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}

trait CalculatorService extends BlueEyesServiceBuilder with BijectionsChunkString {
  val calculatorService = service("calculatorService", "1.0.0") {
    context =>
      startup {
        Promise.success{()}
      } ->
      request { config: Unit =>
        path("/add" / 'number1 / 'number2) {
          try {
            val sum =
              for {
                number1 <- request.parameters.get('number1).toInt
                number2 <- request.parameters.get('number2).toInt
              } yield (number1 + number2).toString

           Promise success {
             HttpResponse[ByteChunk](content = Some(sum.toString))
           }
          } catch {
              case e: NumberFormatException =>
                Promise success {
                  HttpResponse[ByteChunk](status = HttpStatus(BadRequest))
                }
          }
        } ~
        path("/multiply" / 'number1 / 'number2) {
          try {
            val product =
              for {
                number1 <- request.parameters.get('number1).toInt
                number2 <- request.parameters.get('number2).toInt
              } yield (number1 * number2).toString

            Promise success {
              HttpResponse[ByteChunk](content = Some(product.toString))
            }
          } catch {
              case e: NumberFormatException =>
                Promise success {
                  HttpResponse[ByteChunk](status = HttpStatus(BadRequest))
                }
          }
        }
      } ->
      shutdown { config =>
        println("Shutting down")
        Promise.success{()}
      }
  }
}
