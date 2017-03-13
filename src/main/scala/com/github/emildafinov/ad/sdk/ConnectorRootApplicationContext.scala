package com.github.emildafinov.ad.sdk

import com.github.emildafinov.ad.sdk.configuration.ApplicationConfigurationModule
import com.github.emildafinov.ad.sdk.event.{RawEventHandlersModule, RoutingDependenciesModule}
import com.github.emildafinov.ad.sdk.event.parsers.RichEventParsersModule
import com.github.emildafinov.ad.sdk.internal.ClientDefinedDependenciesModule
import com.github.emildafinov.ad.sdk.server._

private[sdk] trait ConnectorRootApplicationContext
  extends ApplicationConfigurationModule
  with AkkaDependenciesModule
  with RichEventParsersModule
  with HealthRoutes
  with RoutingDependenciesModule
  with RawEventHandlersModule
  with EventResultMarshallersModule
  with CustomDirectivesModule
  with AppMarketCommunicationRoutesModule
  with RoutesModule {

  this: ClientDefinedDependenciesModule =>
}
