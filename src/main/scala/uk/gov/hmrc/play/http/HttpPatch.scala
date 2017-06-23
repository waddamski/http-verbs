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

import uk.gov.hmrc.play.http.HttpVerbs.{PATCH => PATCH_VERB}
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.play.http.HttpTransport.CorePatch
import uk.gov.hmrc.play.http.hooks.{HttpHook, HttpHooks}
import uk.gov.hmrc.play.http.logging.ConnectionTracing

import scala.concurrent.{ExecutionContext, Future}

trait HttpPatch extends CorePatch with HttpVerb with ConnectionTracing with HttpHooks {

  def PATCH[I](url: String, body: I)(implicit wts: Writes[I], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    withTracing(PATCH_VERB, url) {
      val httpResponse = doPatch(url, body)
      executeHooks(url, PATCH_VERB, Option(Json.stringify(wts.writes(body))), httpResponse)
      mapErrors(PATCH_VERB, url, httpResponse)
    }
  }
}
