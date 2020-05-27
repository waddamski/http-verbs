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

import play.api.http.Writeable
import play.api.libs.json.{Json, JsValue, Writes => JsonWrites}
import play.api.libs.ws.{InMemoryBody, WSBody}
import play.api.mvc.Results

import com.github.ghik.silencer.silent

trait HttpWritesInstances {

  implicit val fromJson: HttpWrites[JsValue] =
    new HttpWrites[JsValue] {
      override def write(b: JsValue): WSBody =
        InMemoryBody(implicitly[Writeable[JsValue]].transform(b))

      override def writeForHook(b: JsValue): HookData =
        HookDataString(Json.stringify(b))
    }

  implicit def fromJson2[A : JsonWrites]: HttpWrites[A] =
    HttpWrites[JsValue]
      .contramap(Json.toJson[A])

  implicit val fromString: HttpWrites[String] =
    new HttpWrites[String] {
      override def write(b: String): WSBody =
        InMemoryBody(implicitly[Writeable[String]].transform(b))

      override def writeForHook(b: String): HookData =
        HookDataString(b)
    }

  implicit val fromForm: HttpWrites[Map[String, Seq[String]]] =
    new HttpWrites[Map[String, Seq[String]]] {
      override def write(b: Map[String, Seq[String]]): WSBody =
        InMemoryBody(implicitly[Writeable[Map[String, Seq[String]]]].transform(b))

      override def writeForHook(b: Map[String, Seq[String]]): HookData =
        HookDataMap(b)
    }

  implicit val fromEmpty: HttpWrites[Unit] =
    new HttpWrites[Unit] {
      override def write(b: Unit): WSBody =
        InMemoryBody(implicitly[Writeable[Results.EmptyContent]].transform(Results.EmptyContent()))

      override def writeForHook(b: Unit): HookData =
        HookDataEmpty
    }
}