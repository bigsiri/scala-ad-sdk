package com.github.emildafinov.ad.sdk.http.server.routing.directives

import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives.parameter
import com.github.emildafinov.ad.sdk.authentication.AppMarketCredentials
import com.github.emildafinov.ad.sdk.http.client.AppMarketEventFetcher
import com.github.emildafinov.ad.sdk.payload.Event

object SignedFetchDirective {

  /**
    * Directive that performs the signed fetch of the event and makes the payload
    * available tou the inner [[akka.http.scaladsl.server.Route]]s
    * @param eventFetcher an object used to perform the signed fetch of the raw event payload
    * @param clientCredentials the id of the client who sent the request
    * @return
    */
  def apply(eventFetcher: AppMarketEventFetcher, clientCredentials: AppMarketCredentials): Directive[(String, Event)] =
    parameter("eventUrl") map { eventFetchUrl =>
      eventFetcher.fetchRawAppMarketEvent(clientCredentials, eventFetchUrl)
    }
}
