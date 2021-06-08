package controllers

import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneAppPerTest}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends WordSpec with MustMatchers with GuiceOneAppPerSuite with Injecting {

  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      val controller = new HomeController(stubControllerComponents())
      val response = controller.index()(FakeRequest())

      status(response) mustBe OK
      contentType(response) mustBe Some("text/html")
      contentAsString(response) must include ("Welcome to Training!")
    }

    "render the index page from the application" in {
      val controller = inject[HomeController]
      val home = controller.index()(FakeRequest())

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to Training!")
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val response = route(app, request).get

      status(response) mustBe OK
      contentType(response) mustBe Some("text/html")
      contentAsString(response) must include ("Welcome to Training!")
    }
  }
}
