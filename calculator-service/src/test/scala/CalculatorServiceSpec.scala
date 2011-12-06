import blueeyes.core.service.test.BlueEyesServiceSpecification
import blueeyes.core.http.HttpResponse

class CalculatorServiceSpec extends BlueEyesServiceSpecification with CalculatorService {
  "Calculator.add" should {
    "return with the sum of its inputs" in {
      val beStringResponse = be_==(_:Option[String]) ^^ ((_:HttpResponse[String]).content)

      val future = service.get[String]("/add/1/2")
      future.value must eventually(beSomething)
      println(future.value.get)

      service.get[String]("/add/1/2") must whenDelivered {
        beStringResponse(Some("3"))
      }
    }
  }
}

