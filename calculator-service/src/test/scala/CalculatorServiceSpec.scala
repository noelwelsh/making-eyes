import blueeyes.core.service.test.BlueEyesServiceSpecification
import blueeyes.core.http.HttpResponse

class CalculatorServiceSpec extends BlueEyesServiceSpecification with CalculatorService {
  "Calculator.add" should {
    "return with the sum of its inputs" in {
      service.get[String]("/add/1/2") must whenDelivered {
        response: HttpResponse[String] => response.content must beSome("3")
      }
    }
  }
}

