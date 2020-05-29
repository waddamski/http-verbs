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

import com.github.ghik.silencer.silent
import play.api.libs.json.{JsValue, Json}

/**
  * The ws.Response class is very hard to dummy up as it wraps a concrete instance of
  * the ning http Response. This case class exposes just the bits of the response that we
  * need in methods that we are passing the response to for processing, making it
  * much easier to provide dummy data in our specs.
  */
case class HttpResponse(
  status : Int,
  body   : String,
  headers: Map[String, Seq[String]]
) {
  def json: JsValue =
    Json.parse(body)

  def header(key: String): Option[String] =
    headers.get(key).flatMap(_.headOption)

  override def toString: String =
    s"HttpResponse status=$status"
}

object HttpResponse {
  def apply(
    status : Int,
    body   : String
  ): HttpResponse =
    apply(
      status  = status,
      body    = body,
      headers = Map.empty
    )

  def apply(
    status : Int,
    json   : JsValue,
    headers: Map[String, Seq[String]]
  ): HttpResponse =
    apply(
      status  = status,
      body    = Json.prettyPrint(json),
      headers = headers
    )
}
