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

package uk.gov.hmrc.play.http.ws.default

import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.http.{CorePost, HeaderCarrier, HttpResponse, HttpWrites, PostHttpTransport}
import uk.gov.hmrc.play.http.ws.{WSExecute, WSHttpResponse, WSRequestBuilder}

import scala.concurrent.{ExecutionContext, Future}

trait WSPost extends CorePost with PostHttpTransport with WSRequestBuilder with WSExecute {

  def withEmptyBody(request: WSRequest): WSRequest

  override def doPost2[A](
    url: String,
    body: A,
    headers: Seq[(String, String)]
  )(
    implicit writes: HttpWrites[A],
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    execute(buildRequest(url, headers).withBody(writes.write(body)), "POST")
      .map(WSHttpResponse.apply)

  @deprecated("Use doPost2 instead", "11.0.0")
  override def doPost[A](
    url: String,
    body: A,
    headers: Seq[(String, String)]
  )(
    implicit wts: Writes[A],
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    doPost2[A](url, body, headers)

  @deprecated("Use doPost2 instead", "11.0.0")
  override def doFormPost(
    url: String,
    body: Map[String, Seq[String]],
    headers: Seq[(String, String)]
  )(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    doPost2[Map[String, Seq[String]]](url, body, headers)

  @deprecated("Use doPost2 instead", "11.0.0")
  override def doPostString(
    url: String,
    body: String,
    headers: Seq[(String, String)]
  )(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    doPost2[String](url, body, headers)

  @deprecated("Use doPost2 instead", "11.0.0")
  override def doEmptyPost[A](
    url: String,
    headers: Seq[(String, String)]
  )(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    doPost2[Unit](url, (), headers)
}
