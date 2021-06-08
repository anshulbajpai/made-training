package controllers

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.WsScalaTestClient
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Injecting

class IndexIntegrationSpec extends WordSpec with GuiceOneServerPerSuite with MustMatchers with WsScalaTestClient with ScalaFutures with Injecting  {

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(150, Millis))
  )

  private implicit val wsClient = inject[WSClient]

  "GET /" must {
    "render welcome message" in {
      val response: WSResponse = wsUrl("/").get().futureValue

      response.status mustBe OK
      response.body must include("Welcome to Training!")
    }
  }

}
