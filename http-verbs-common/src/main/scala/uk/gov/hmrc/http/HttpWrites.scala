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

import play.api.libs.ws.WSRequest

import com.github.ghik.silencer.silent

object HttpWrites extends HttpWritesInstances {
  def apply[A : HttpWrites] =
    implicitly[HttpWrites[A]]
}

// the hook stuff is really just to wire up with `play-auditing`, but takes the form Option[Any]...
// see [[HttpAuditing.maskRequestBody]] for unenforced expectations about the type...
// quoted here:
  //The request body comes from calls to executeHooks in http-verbs
  //It is either called with
  // - a Map in the case of a web form
  // - a String created from Json.stringify(wts.writes(... in the case of a class
  // - a String in the case of a string (where the string can be XML)
sealed trait HookData { def toOptionAny: Option[_] }
case object HookDataEmpty extends HookData { override def toOptionAny: Option[_] = None }
case class HookDataString(s: String) extends HookData  { override def toOptionAny: Option[_] = Some(s) }
case class HookDataMap(m: Map[String, Seq[String]]) extends HookData  { override def toOptionAny: Option[_] = Some(m) }

trait HttpWrites[A] {
  outer =>

  // TODO instances need to be defined per play version, since api has changed since Play 2.5 and 2.6
  // However, even WSBody isn't available? Change to  `WSRequest => WsRequest`? (What is most symmetric to HttpResponse => A ?)
  def write(wsRequest: WSRequest, a: A): WSRequest

  def toHookData(a: A): HookData

  def contramap[B](fn: B => A): HttpWrites[B] =
    new HttpWrites[B] {
      override def write(wsRequest: WSRequest, b: B): WSRequest =
        outer.write(wsRequest, fn(b))

      override def toHookData(b: B): HookData =
        outer.toHookData(fn(b))
    }
}
