/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.play.http

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.http.HttpVerbs._
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.test.Concurrent.await
import uk.gov.hmrc.play.test.TestHttpTransport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HttpPostSpec extends WordSpecLike with Matchers with CommonHttpBehaviour with OptionValues  {

  class StubbedHttpPost(doPostResult: Future[HttpResponse]) extends HttpPost with MockitoSugar with ConnectionTracingCapturing with TestHttpTransport {
    val testHook1 = mock[HttpHook]
    val testHook2 = mock[HttpHook]
    val hooks = Seq(testHook1, testHook2)

    override def doPost[A](url: String, body: A, headers: Seq[(String,String)])(implicit rds: Writes[A], hc: HeaderCarrier) = doPostResult
    override def doFormPost(url: String, body: Map[String, Seq[String]])(implicit hc: HeaderCarrier) = doPostResult
    override def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier) = doPostResult
    override def doEmptyPost[A](url: String)(implicit hc: HeaderCarrier) = doPostResult
  }

  "HttpPost.POST" should {
    val testObject = TestRequestClass("a", 1)
    "be able to return plain responses" in {
      val response = new DummyHttpResponse(testBody, 200)
      val testPOST = new StubbedHttpPost(Future.successful(response))
      testPOST.post(url, testObject).futureValue shouldBe response
    }
    "be able to return objects deserialised from JSON" in {
      val testPOST = new StubbedHttpPost(Future.successful(new DummyHttpResponse("""{"foo":"t","bar":10}""", 200)))
      Json.parse(testPOST.post[TestRequestClass](url, testObject).futureValue.body).asOpt[TestClass].value should be (TestClass("t", 10))
    }

    behave like anErrorMappingHttpCall(POST, (url, responseF) => new StubbedHttpPost(responseF).post(url, testObject))
    behave like aTracingHttpCall(POST, "POST", new StubbedHttpPost(defaultHttpResponse)) { _.post(url, testObject) }

    "Invoke any hooks provided" in {
      val dummyResponseFuture = Future.successful(new DummyHttpResponse(testBody, 200))
      val testPatch = new StubbedHttpPost(dummyResponseFuture)
      await(testPatch.post(url, testObject))

      val testJson = Json.stringify(trcreads.writes(testObject))

      verify(testPatch.testHook1)(url, "POST", Some(testJson), dummyResponseFuture)
      verify(testPatch.testHook2)(url, "POST", Some(testJson), dummyResponseFuture)
    }
  }

  "HttpPost.POSTForm" should {
    "be able to return plain responses" in {
      val response = new DummyHttpResponse(testBody, 200)
      val testPOST = new StubbedHttpPost(Future.successful(response))
      testPOST.postForm(url, Map()).futureValue shouldBe response
    }
    "be able to return objects deserialised from JSON" in {
      val testPOST = new StubbedHttpPost(Future.successful(new DummyHttpResponse("""{"foo":"t","bar":10}""", 200)))
      Json.parse(testPOST.postForm(url, Map()).futureValue.body).asOpt[TestClass].value should be (TestClass("t", 10))
    }

    behave like anErrorMappingHttpCall(POST, (url, responseF) => new StubbedHttpPost(responseF).postForm(url, Map()))
    behave like aTracingHttpCall(POST, "POST", new StubbedHttpPost(defaultHttpResponse)) { _.postForm(url, Map()) }

    "Invoke any hooks provided" in {
      val dummyResponseFuture = Future.successful(new DummyHttpResponse(testBody, 200))
      val testPost = new StubbedHttpPost(dummyResponseFuture)
      await(testPost.postForm(url, Map()))

      verify(testPost.testHook1)(url, "POST", Some(Map()), dummyResponseFuture)
      verify(testPost.testHook2)(url, "POST", Some(Map()), dummyResponseFuture)
    }
  }

  "HttpPost.POSTString" should {
    "be able to return plain responses" in {
      val response = new DummyHttpResponse(testBody, 200)
      val testPOST = new StubbedHttpPost(Future.successful(response))
      testPOST.postString(url, testRequestBody).futureValue shouldBe response
    }
    "be able to return objects deserialised from JSON" in {
      val testPOST = new StubbedHttpPost(Future.successful(new DummyHttpResponse("""{"foo":"t","bar":10}""", 200)))
      Json.parse(testPOST.postString(url, testRequestBody).futureValue.body).asOpt[TestClass].value should be (TestClass("t", 10))
    }

    behave like anErrorMappingHttpCall(POST, (url, responseF) => new StubbedHttpPost(responseF).postString(url, testRequestBody))
    behave like aTracingHttpCall(POST, "POST", new StubbedHttpPost(defaultHttpResponse)) { _.postString(url, testRequestBody) }

    "Invoke any hooks provided" in {
      val dummyResponseFuture = Future.successful(new DummyHttpResponse("""{"foo":"t","bar":10}""", 200))
      val testPost = new StubbedHttpPost(dummyResponseFuture)
      await(testPost.postString(url, testRequestBody))

      verify(testPost.testHook1)(url, "POST", Some(testRequestBody), dummyResponseFuture)
      verify(testPost.testHook2)(url, "POST", Some(testRequestBody), dummyResponseFuture)
    }
  }

  "HttpPost.POSTEmpty" should {
    "be able to return plain responses" in {
      val response = new DummyHttpResponse(testBody, 200)
      val testPOST = new StubbedHttpPost(Future.successful(response))
      testPOST.postEmpty(url).futureValue shouldBe response
    }
    "be able to return objects deserialised from JSON" in {
      val testPOST = new StubbedHttpPost(Future.successful(new DummyHttpResponse("""{"foo":"t","bar":10}""", 200)))
      Json.parse(testPOST.postEmpty(url).futureValue.body).asOpt[TestClass].value should be (TestClass("t", 10))
    }

    behave like anErrorMappingHttpCall(POST, (url, responseF) => new StubbedHttpPost(responseF).postEmpty(url))
    behave like aTracingHttpCall(POST, "POST", new StubbedHttpPost(defaultHttpResponse)) { _.postEmpty(url) }

    "Invoke any hooks provided" in {
      val dummyResponseFuture = Future.successful(new DummyHttpResponse("""{"foo":"t","bar":10}""", 200))
      val testPost = new StubbedHttpPost(dummyResponseFuture)
      await(testPost.postEmpty(url))

      verify(testPost.testHook1)(url, "POST", None, dummyResponseFuture)
      verify(testPost.testHook2)(url, "POST", None, dummyResponseFuture)
    }
  }

  "POSTEmpty"  should {
    behave like anErrorMappingHttpCall(POST, (url, responseF) => new StubbedHttpPost(responseF).postEmpty(url))
    behave like aTracingHttpCall[StubbedHttpPost](POST, "POSTEmpty", new StubbedHttpPost(defaultHttpResponse)) { _.postEmpty(url) }
  }

}
