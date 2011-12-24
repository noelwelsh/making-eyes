import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.{HttpRequest, HttpResponse, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}

trait CalculatorService extends BlueEyesServiceBuilder with BijectionsChunkString {
  val calculatorService = service("calculatorService", "1.0.0") {
    context => 
      startup {
        ().future
      } ->
      request { config: Unit =>
        path("/add" / 'number1 / 'number2) { 
          parameter('number1) { 
            parameter('number2) {
              service { request: HttpRequest[ByteChunk] =>
                number2: String => number1: String =>
                  try {
                    //val number1 = request.parameters.get('number1).get
                    //val number2 = request.parameters.get('number2).get
                    val n1 = number1.toInt
                    val n2 = number2.toInt
                    val sum = n1 + n2

                    HttpResponse[ByteChunk](content = Some(sum.toString)).future
                  } catch {
                    case e => HttpResponse[ByteChunk](status = HttpStatus(BadRequest)).future  
                  }
              }
            }
          }
        } ~
        path("/multiply" / 'number1 / 'number2) {
          parameter('number1) { 
            parameter('number2) { 
              service { request: HttpRequest[ByteChunk] =>
                number2: String => number1: String =>
                try {
                  val n1 = number1.toInt
                  val n2 = number2.toInt
                  val product = n1 * n2
                
                  HttpResponse[ByteChunk](content = Some(product.toString)).future
                } catch {
                  case e: NumberFormatException => HttpResponse[ByteChunk](status = HttpStatus(BadRequest)).future
                }
              }
            }
          }
        } 
      } ->
      shutdown { config =>
        println("Shutting down")
        ().future
      }
  }
}
