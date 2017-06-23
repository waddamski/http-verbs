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

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.play.http.HttpTransport.CorePost
import uk.gov.hmrc.play.http.HttpVerbs.{POST => POST_VERB}
import uk.gov.hmrc.play.http.hooks.HttpHooks
import uk.gov.hmrc.play.http.logging.ConnectionTracing

import scala.concurrent.{ExecutionContext, Future}

trait HttpPost extends CorePost with HttpVerb with ConnectionTracing with HttpHooks {

  def POST[I](url: String, body: I, headers: Seq[(String,String)] = Seq.empty)(implicit wts: Writes[I], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    withTracing(POST_VERB, url) {
      val httpResponse = doPost(url, body, headers)
      executeHooks(url, POST_VERB, Option(Json.stringify(wts.writes(body))), httpResponse)
      mapErrors(POST_VERB, url, httpResponse)
    }
  }

  def POSTString(url: String, body: String, headers: Seq[(String,String)] = Seq.empty)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    withTracing(POST_VERB, url) {
      val httpResponse = doPostString(url, body, headers)
      executeHooks(url, POST_VERB, Option(body), httpResponse)
      mapErrors(POST_VERB, url, httpResponse)
    }
  }

  def POSTForm(url: String, body: Map[String, Seq[String]])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    withTracing(POST_VERB, url) {
      val httpResponse = doFormPost(url, body)
      executeHooks(url, POST_VERB, Option(body), httpResponse)
      mapErrors(POST_VERB, url, httpResponse)
    }
  }

  def POSTEmpty(url: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    withTracing(POST_VERB, url) {
      val httpResponse = doEmptyPost(url)
      executeHooks(url, POST_VERB, None, httpResponse)
      mapErrors(POST_VERB, url, httpResponse)
    }
  }
}
