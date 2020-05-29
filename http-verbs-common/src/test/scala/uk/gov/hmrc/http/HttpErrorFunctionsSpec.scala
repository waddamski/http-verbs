/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.http

import org.scalacheck.Gen
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.EitherValues
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.util.Try

class HttpErrorFunctionsSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckDrivenPropertyChecks
    with TableDrivenPropertyChecks
    with EitherValues {

  "HttpErrorFunctions" should {
    "return an UpstreamErrorResponse if the status code is between 400 and 499" in new HttpErrorFunctions {
      forAll(Gen.choose(400, 499)) { statusCode: Int =>
        val expectedResponse = HttpResponse(statusCode, exampleBody)
        val r = handleResponseEither(exampleVerb, exampleUrl)(expectedResponse)
        val error = handleResponseEither(exampleVerb, exampleUrl)(expectedResponse).left.value
        error.getMessage should (include(exampleUrl) and include(exampleVerb) and include(expectedResponse.body))
        error should have('statusCode (statusCode))
      }
    }

    "return an UpstreamErrorResponse if the status code is between 500 and 599" in new HttpErrorFunctions {
      forAll(Gen.choose(500, 599)) { statusCode: Int =>
        val expectedResponse = HttpResponse(statusCode, exampleBody)
        val error = handleResponseEither(exampleVerb, exampleUrl)(expectedResponse).left.value
        error.getMessage should (include(exampleUrl) and include(exampleVerb) and include(expectedResponse.body))
        error should have('statusCode (statusCode))
      }
    }

    "return the response if the status code is outside 400 and 599" in new HttpErrorFunctions {
      forAll(Gen.choose(0, 399)) { statusCode: Int =>
        val expectedResponse = HttpResponse(statusCode, "")
        handleResponseEither(exampleVerb, exampleUrl)(expectedResponse) shouldBe Right(expectedResponse)
      }

      forAll(Gen.choose(600, 1000)) { statusCode: Int =>
        val expectedResponse = HttpResponse(statusCode, "")
        handleResponseEither(exampleVerb, exampleUrl)(expectedResponse) shouldBe Right(expectedResponse)
      }
    }
  }

  val exampleVerb = "GET"
  val exampleUrl  = "http://example.com/something"
  val exampleBody = "this is the string body"
}
