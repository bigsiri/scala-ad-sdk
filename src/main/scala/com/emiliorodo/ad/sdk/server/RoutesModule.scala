package com.emiliorodo.ad.sdk.server

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import com.emiliorodo.ad.sdk.AkkaDependenciesModule
import com.emiliorodo.ad.sdk.configuration.ApplicationConfigurationModule
import com.emiliorodo.ad.sdk.events.parsers.RichEventParsersModule
import com.emiliorodo.ad.sdk.internal.ClientDefinedEventHandlersModule
import com.emiliorodo.ad.sdk.payload.{Event, EventJsonSupport}

import scala.concurrent.Future

private[sdk] trait RoutesModule extends Directives with EventJsonSupport {

  this: ApplicationConfigurationModule 
   with HealthRoutes 
   with ClientDefinedEventHandlersModule 
   with RichEventParsersModule 
   with AkkaDependenciesModule =>

  lazy val baseRoute: Route =
    handleExceptions(rootExceptionHandler) {
      health ~ appmarketIntegrationRoutes
    }

  lazy val rootExceptionHandler = ExceptionHandler {
      case _: ArithmeticException =>
        complete(HttpResponse(BadRequest, entity = "The operation you requested is not supported"))
    }
  
  def appmarketIntegrationRoutes: Route =
    (pathPrefix("integration") & entity(as[Event])) { implicit event =>
      subscriptionOrder ~ subscriptionCancel ~ subscriptionChange ~ subscriptionNotice ~
      addonOrder ~ addonCancel ~
      userAssignment ~ userUnassignment
    }

  def subscriptionOrder(implicit event: Event): Route =
    path("subscription" / "order") {
      complete {
        Future {
          subscriptionOrderHandler.handle(subscriptionOrderEventParser(event))
        }
      }
    }

  def subscriptionCancel(implicit event: Event): Route =
    path("subscription" / "cancel") {
      complete(???)
    }

  def subscriptionChange(implicit event: Event): Route =
    path("subscription" / "change") {
      complete(???)
    }

  def subscriptionNotice(implicit event: Event): Route =
    path("subscription" / "notice") {
      complete(???)
    }

  def addonOrder(implicit event: Event): Route =
    path("subscription" / "addon" / "order") {
      complete(???)
    }

  def addonCancel(implicit event: Event): Route =
    path("subscription" / "addon" / "cancel") {
      complete(???)
    }

  def userAssignment(implicit event: Event): Route =
    path("user" / "assignment") {
      complete(???)
    }

  def userUnassignment(implicit event: Event): Route =
    path("user" / "unassignment") {
      complete(???)
    }
}



