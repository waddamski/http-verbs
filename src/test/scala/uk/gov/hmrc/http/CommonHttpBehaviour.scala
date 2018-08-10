/*
 * Copyright 2018 HM Revenue & Customs
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

import java.net.ConnectException
import java.util.concurrent.TimeoutException

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.json
import play.api.libs.json._
import uk.gov.hmrc.http.logging.{ConnectionTracing, LoggingDetails}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait CommonHttpBehaviour extends ScalaFutures with Matchers with WordSpecLike {

  case class TestClass(foo: String, bar: Int)
  implicit val tcreads = Json.format[TestClass]

  case class TestRequestClass(baz: String, bar: Int)
  implicit val trcreads = Json.format[TestRequestClass]

  implicit val hc     = HeaderCarrier()
  val testBody        = "testBody"
  val testRequestBody = "testRequestBody"
  val url             = "http://some.url"

  def response(returnValue: Option[String] = None, statusCode: Int = 200) =
    Future.successful(HttpResponse(statusCode, responseString = returnValue))

  val defaultHttpResponse = response()

  def anErrorMappingHttpCall(verb: String, httpCall: (String, Future[HttpResponse]) => Future[_]) = {
    s"throw a GatewayTimeout exception when the HTTP $verb throws a TimeoutException" in {

      implicit val hc = HeaderCarrier()
      val url: String = "http://some.nonexistent.url"

      val e = httpCall(url, Future.failed(new TimeoutException("timeout"))).failed.futureValue

      e            should be(a[GatewayTimeoutException])
      e.getMessage should startWith(verb)
      e.getMessage should include(url)
    }

    s"throw a BadGateway exception when the HTTP $verb throws a ConnectException" in {

      implicit val hc = HeaderCarrier()
      val url: String = "http://some.nonexistent.url"

      val e = httpCall(url, Future.failed(new ConnectException("timeout"))).failed.futureValue

      e            should be(a[BadGatewayException])
      e.getMessage should startWith(verb)
      e.getMessage should include(url)
    }
  }

  def aTracingHttpCall[T <: ConnectionTracingCapturing](verb: String, method: String, httpBuilder: => T)(
    httpAction: (T => Future[_]))(implicit mf: Manifest[T]) =
    s"trace exactly once when the HTTP $verb calls $method" in {
      val http = httpBuilder
      httpAction(http).futureValue
      http.traceCalls         should have size 1
      http.traceCalls.head._1 shouldBe verb
    }

}

object JsonHttpReads extends HttpErrorFunctions {
  implicit def writeJson[I](implicit wts: json.Writes[I]) = new HttpWrites[I] {
    override def write(body: I): String = Json.stringify(wts.writes(body))
  }

  implicit def readFromJson[O](implicit rds: json.Reads[O], mf: Manifest[O]): HttpReads[O] = new HttpReads[O] {
    def read(method: String, url: String, response: HttpResponse) =
      readJson(method, url, Json.parse(handleResponse(method, url)(response).body))
  }

  def readSeqFromJsonProperty[O](name: String)(implicit rds: json.Reads[O], mf: Manifest[O]) = new HttpReads[Seq[O]] {
    def read(method: String, url: String, response: HttpResponse) = response.status match {
      case 204 | 404 => Seq.empty
      case _ =>
        readJson[Seq[O]](method, url, (Json.parse(handleResponse(method, url)(response).body) \ name).getOrElse(JsNull)) //Added JsNull here to force validate to fail - replicates existing behaviour
    }
  }

  private def readJson[A](method: String, url: String, jsValue: JsValue)(implicit rds: json.Reads[A], mf: Manifest[A]) =
    jsValue
      .validate[A]
      .fold(
        errs => throw new JsValidationException(method, url, mf.runtimeClass, errs.toString()),
        valid => valid
      )
}

trait ConnectionTracingCapturing extends ConnectionTracing {

  val traceCalls = mutable.Buffer[(String, String)]()

  override def withTracing[T](method: String, uri: String)(
    body: => Future[T])(implicit ld: LoggingDetails, ec: ExecutionContext) = {
    traceCalls += ((method, uri))
    body
  }
}
