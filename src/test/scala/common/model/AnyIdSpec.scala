package common.model

import cats.data.Validated.Valid
import fixtures.{ApiTestId, TestId}
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.*

class AnyIdSpec extends AnyFlatSpec with Matchers {
  "Any Id Type instance " should "successfully create id instances from valid string" in {
    ApiTestId.parse("test_1") should equal (Valid(TestId(1)))
  }
}
