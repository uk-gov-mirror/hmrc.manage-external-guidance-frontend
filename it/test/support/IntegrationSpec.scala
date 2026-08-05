/*
 * Copyright 2023 HM Revenue & Customs
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

package support

import org.scalatest.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSRequest, WSResponse}
import play.api.mvc.{Session, SessionCookieBaker}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.{HeaderNames, SessionKeys}
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto

trait IntegrationSpec
    extends AnyWordSpecLike
    with EitherValues
    with Matchers
    with FutureAwaits
    with DefaultAwaitTimeout
    with WireMockHelper
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

  val mockHost: String = WireMockHelper.host
  val mockPort: String = WireMockHelper.wireMockPort.toString
  val SessionId: String = s"mock-sessionid"
  val BearerToken: String = "mock-bearer-token"
  private val rootContext = "/external-guidance"
  private val servicesPath = "microservice.services"

  lazy val client: WSClient = app.injector.instanceOf[WSClient]
  lazy val cookieCrypto: SessionCookieCrypto = app.injector.instanceOf[SessionCookieCrypto]
  lazy val cookieBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]

  def overriddenConfig: Map[String, Any] = Map(
    s"$servicesPath.auth.host" -> mockHost,
    s"$servicesPath.auth.port" -> mockPort,
    s"$servicesPath.external-guidance.host" -> mockHost,
    s"$servicesPath.external-guidance.port" -> mockPort,
    "auditing.consumer.baseUri.port" -> mockPort,
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck"
  )

  override given app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(overriddenConfig)
    .build()

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWireMock()
  }

  override def afterAll(): Unit = {
    stopWireMock()
    super.afterAll()
  }

  def buildRequest(path: String): WSRequest = {
    val headers = List(
      HeaderNames.xSessionId -> SessionId,
      HeaderNames.authorisation -> BearerToken,
      "Csrf-Token" -> "nocheck"
    )
    client
      .url(s"http://localhost:$port$rootContext$path")
      .withCookies(mockSessionCookie)
      .withHttpHeaders(headers:_*)
      .withFollowRedirects(false)
  }

  def document(response: WSResponse): JsValue = Json.parse(response.body)

  def mockSessionCookie: DefaultWSCookie = {
    val sessionCookie = cookieBaker.encodeAsCookie(Session(Map(
      SessionKeys.lastRequestTimestamp -> System.currentTimeMillis().toString,
      SessionKeys.authToken -> BearerToken,
      SessionKeys.sessionId -> SessionId
    )))

    DefaultWSCookie(
      sessionCookie.name,
      cookieCrypto.crypto.encrypt(PlainText(sessionCookie.value)).value,
      sessionCookie.domain,
      Some(sessionCookie.path),
      sessionCookie.maxAge.map(_.toLong),
      sessionCookie.secure,
      sessionCookie.httpOnly
    )
  }
}
