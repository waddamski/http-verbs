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

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.HttpVerbs.{POST => POST_VERB}
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.http.logging.ConnectionTracing

import scala.concurrent.{ExecutionContext, Future}

trait HttpPost
    extends CorePost
    with PostHttpTransport
    with HttpVerb
    with ConnectionTracing
    with HttpHooks
    with Retries {

  override def POST2[I, O](
    url    : String,
    body   : I,
    headers: Seq[(String, String)]
  )(implicit
    wts: HttpWrites[I],
    rds: HttpReads[O],
    hc : HeaderCarrier,
    ec : ExecutionContext
  ): Future[O] =
    withTracing(POST_VERB, url) {
      val httpResponse = retry(POST_VERB, url)(doPost2(url, body, headers))
      executeHooks(url, POST_VERB, wts.toHookData(body).toOptionAny, httpResponse)
      mapErrors(POST_VERB, url, httpResponse)
        .map(rds.read(POST_VERB, url, _))
    }

  @deprecated("Use POST2 instead.", "11.0.0")
  override def POST[I, O](
    url    : String,
    body   : I,
    headers: Seq[(String, String)]
  )(implicit
    wts: Writes[I],
    rds: HttpReads[O],
    hc : HeaderCarrier,
    ec : ExecutionContext
  ): Future[O] =
    POST2[I, O](url, body, headers)

  @deprecated("Use POST2 instead.", "11.0.0")
  override def POSTString[O](
    url    : String,
    body   : String,
    headers: Seq[(String, String)]
  )(implicit
    rds: HttpReads[O],
    hc : HeaderCarrier,
    ec : ExecutionContext
  ): Future[O] =
    POST2[String, O](url, body, headers)

  @deprecated("Use POST2 instead.", "11.0.0")
  override def POSTForm[O](
    url    : String,
    body   : Map[String, Seq[String]],
    headers: Seq[(String, String)]
  )(implicit
    rds: HttpReads[O],
    hc : HeaderCarrier,
    ec : ExecutionContext
  ): Future[O] =
    POST2[Map[String, Seq[String]], O](url, body, headers)

  @deprecated("Use POST2 instead.", "11.0.0")
  override def POSTEmpty[O](
    url    : String,
    headers: Seq[(String, String)]
  )(implicit
    rds: HttpReads[O],
    hc : HeaderCarrier,
    ec : ExecutionContext
  ): Future[O] =
    POST2[Unit, O](url, (), headers)
}
