package com.github.emildafinov.ad.sdk.event.resolvers

import com.github.emildafinov.ad.sdk.AkkaDependenciesModule
import com.github.emildafinov.ad.sdk.authentication.AuthorizationTokenGenerator
import com.github.emildafinov.ad.sdk.event.SdkProvidedEventResolver
import com.github.emildafinov.ad.sdk.event.marshallers.EventResultMarshallersModule
import com.github.emildafinov.ad.sdk.http.client.AppMarketEventResolver
import com.github.emildafinov.ad.sdk.internal.ClientDefinedCredentialsModule

trait EventResolversModule {
  this: ClientDefinedCredentialsModule with EventResultMarshallersModule with AkkaDependenciesModule =>

  lazy val authorizationTokenGenerator = new AuthorizationTokenGenerator
  lazy val eventResolver = new AppMarketEventResolver(authorizationTokenGenerator, credentialsSupplier)
  lazy val subscriptionOrderResolver = new SdkProvidedEventResolver(eventResolver, subscriptionOrderResponseMarshaller)
}