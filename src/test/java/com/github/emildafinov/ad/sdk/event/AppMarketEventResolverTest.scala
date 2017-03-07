package com.github.emildafinov.ad.sdk.event

import java.util.Optional

import com.github.emildafinov.ad.sdk.authentication.{AuthorizationTokenGenerator, CredentialsSupplier, MarketplaceCredentials}
import com.github.emildafinov.ad.sdk.payload.ApiResults
import com.github.emildafinov.ad.sdk.{AkkaSpec, UnitTestSpec, WiremockHttpServiceTestSuite}
import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.Mockito.when
import org.scalatest.concurrent.PatienceConfiguration.Timeout

class AppMarketEventResolverTest extends UnitTestSpec with AkkaSpec with WiremockHttpServiceTestSuite {

  behavior of "AppMarketEventResolver"

  val credentialsSupplier: CredentialsSupplier = mock[CredentialsSupplier]

  val authorizationTokenGenerator: AuthorizationTokenGenerator = mock[AuthorizationTokenGenerator]

  val tested = new AppMarketEventResolver(authorizationTokenGenerator, credentialsSupplier)

  it should "send a failed `event resolved` callback" in {
    //Given
    val testEventId = "1234qwer"
    val testEventProcessingResult = ApiResults.unknownError()
    val testKey = "testKey"
    val testSecret = "testSecret"
    val testEventResolutionEndpoint = s"http://localhost:${httpServerMock.port()}"

    val testClientCredentials = ClientCredentials(
      clientKey = testKey,
      clientSecret = testSecret
    )

    when {
      credentialsSupplier.readCredentialsFor(testKey)
    } thenReturn testClientCredentials

    when {
      authorizationTokenGenerator.generateAuthorizationHeader(
        httpMethodName = "POST",
        resourceUrl = testEventResolutionEndpoint + "/api/integration/v1/events/1234qwer/result",
        marketplaceCredentials = testClientCredentials
      )
    } thenReturn
      s"""OAuth oauth_consumer_key="$testKey", oauth_nonce="abcder", oauth_signature="fgbhndr6yhdrtgf", oauth_signature_method="HMAC-SHA1", oauth_timestamp="1404540540", oauth_version="1.0""""

    httpServerMock.
      givenThat(
        post(urlPathEqualTo(s"/api/integration/v1/events/$testEventId/result"))
          .withHeader("Authorization", matching(s"""OAuth oauth_consumer_key="$testKey", oauth_nonce=".*", oauth_signature=".*", oauth_signature_method="HMAC-SHA1", oauth_timestamp=".*", oauth_version="1.0""""))
          .willReturn {
            aResponse().withStatus(200)
          }
      )

    //When
    val eventNotificationFuture = tested.sendEventResolvedCallback(
      resolveEndpointBaseUrl = testEventResolutionEndpoint,
      eventId = testEventId,
      clientKey = testKey,
      eventProcessingResult = testEventProcessingResult
    )

    //Then
    whenReady(
      future = eventNotificationFuture,
      timeout = Timeout(5 seconds)
    ) { _ =>
      httpServerMock
        .verify(
          postRequestedFor(
            urlPathEqualTo(s"/api/integration/v1/events/$testEventId/result")
          )
        )
    }
  }

  it should "send a successful `event resolved` callback" in {
    //Given
    val testEventId = "1234qwer"
    val testEventProcessingResult = ApiResults.success()
    val testClientKey = "testClientKey"
    val testSecret = "testSecret"
    val testEventResolutionEndpoint = s"http://localhost:${httpServerMock.port()}"

    val testClientCredentials = ClientCredentials(
      clientKey = testClientKey,
      clientSecret = testSecret
    )
    when {
      credentialsSupplier.readCredentialsFor(testClientKey)
    } thenReturn ClientCredentials(
        clientKey = testClientKey,
        clientSecret = testSecret
      )

    when {
      authorizationTokenGenerator.generateAuthorizationHeader(
        httpMethodName = "POST",
        resourceUrl = testEventResolutionEndpoint + "/api/integration/v1/events/1234qwer/result",
        marketplaceCredentials = testClientCredentials
      )
    } thenReturn
      s"""OAuth oauth_consumer_key="$testClientKey", oauth_nonce="abcder", oauth_signature="fgbhndr6yhdrtgf", oauth_signature_method="HMAC-SHA1", oauth_timestamp="1404540540", oauth_version="1.0""""

    httpServerMock.
      givenThat(
        post(urlPathEqualTo(s"/api/integration/v1/events/$testEventId/result"))
          .withHeader("Authorization", containing("OAuth "))
          .willReturn {
            aResponse().withStatus(200)
          }
      )

    //When
    val eventNotificationFuture = tested.sendEventResolvedCallback(
      resolveEndpointBaseUrl = s"http://localhost:${httpServerMock.port()}",
      eventId = testEventId,
      clientKey = testClientKey,
      eventProcessingResult = testEventProcessingResult
    )

    //Then
    whenReady(
      future = eventNotificationFuture,
      timeout = Timeout(5 seconds)
    ) { _ =>
      httpServerMock
        .verify(
          postRequestedFor(
            urlPathEqualTo(s"/api/integration/v1/events/$testEventId/result")
          )
        )
    }
  }
}