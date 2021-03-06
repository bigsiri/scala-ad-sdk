package com.github.emildafinov.ad.sdk

import java.util.Optional

import com.github.emildafinov.ad.sdk.authentication.{AppMarketCredentials, AppMarketCredentialsImpl, AppMarketCredentialsSupplier, AuthorizationTokenGenerator}
import com.github.emildafinov.ad.sdk.http.client.AppMarketEventFetcher
import com.github.emildafinov.ad.sdk.payload.EventType.{SUBSCRIPTION_NOTICE, SUBSCRIPTION_ORDER}
import com.github.emildafinov.ad.sdk.payload._
import com.github.emildafinov.ad.sdk.util.readResourceFile
import com.github.tomakehurst.wiremock.client.WireMock.{get, _}
import org.mockito.Mockito.{reset, when}

import scala.io.Source
import scala.language.postfixOps

class AppMarketEventFetcherITTest extends ITTestSpec {

  behavior of "AppMarketEventFetcher"

  private val mockCredentialsSuppler = mock[AppMarketCredentialsSupplier]
  private val mockAuthorizationTokenGenerator = mock[AuthorizationTokenGenerator]

  val tested: AppMarketEventFetcher = new AppMarketEventFetcher(authorizationTokenGenerator = mockAuthorizationTokenGenerator)

  before {
    reset(mockCredentialsSuppler, mockAuthorizationTokenGenerator)
  }

  it should "throw an appropriate exception when no credentials are found for the key" in {
    //Given
    val testEventUrl = "http://example.org"
    val testClientKey = "testKey"
    val testClientSecret = "testSecret"
    val testCredentials = AppMarketCredentialsImpl(testClientKey, testClientSecret)

    when {
      mockCredentialsSuppler.readCredentialsFor(testClientKey)
    } thenThrow new RuntimeException()


    //Then
    a[RuntimeException] should be thrownBy {
      //When
      tested.fetchRawAppMarketEvent(testCredentials, testEventUrl)
    }
  }


  it should "parse a raw subscription order event" in {
    //Given
    val testEventId = "someEventIdHere"
    val testClientKey = "testKey"
    val testHost = s"http://localhost:${httpServerMock.port()}"
    val testHttpResource = s"/events/$testEventId"
    val testEventUrl = testHost + testHttpResource

    val testClientSecret = "abcdef"
    val testAppmarketCredentials = AppMarketCredentialsImpl(clientKey = testClientKey, clientSecret = testClientSecret)

    val expectedEventPayloadJson = Source.fromURL(getClass.getResource("/event/subscription_order_event_payload.json")).mkString

    when {
      mockCredentialsSuppler.readCredentialsFor(testClientKey)
    } thenReturn Optional.of[AppMarketCredentials](testAppmarketCredentials)

    when {
      mockAuthorizationTokenGenerator.generateAuthorizationHeaderValue(
        "GET", testEventUrl, testAppmarketCredentials
      )
    } thenReturn "afscgg"

    httpServerMock
      .givenThat {
        get(urlEqualTo(testHttpResource)) willReturn {
          aResponse withBody expectedEventPayloadJson
        }
      }

    val expectedEvent = Event(
      `type` = SUBSCRIPTION_ORDER,
      marketplace = Marketplace(
        partner = "APPDIRECT",
        baseUrl = "http://sample.appdirect.com"
      ),
      creator = User(),
      payload = Payload(
        company = Company(),
        account = None,
        notice = None
      )
    )

    //When
    val (parsedEventId, parsedEvent) = tested.fetchRawAppMarketEvent(testAppmarketCredentials, testEventUrl)

    //Then
    parsedEvent shouldEqual expectedEvent
    parsedEventId shouldEqual testEventId
  }

  it should "parse a raw subscription notice event with type 'closed' " in {
    //Given
    val testEventId = "someEventIdHere"
    val testClientKey = "testKey"
    val testHost = s"http://localhost:${httpServerMock.port()}"
    val testHttpResource = s"/events/$testEventId"
    val testEventUrl = testHost + testHttpResource

    val testClientSecret = "abcdef"
    val testAppmarketCredentials = AppMarketCredentialsImpl(clientKey = testClientKey, clientSecret = testClientSecret)

    val expectedEventPayloadJson: String = readResourceFile("/event/subscription_closed_event_payload.json")

    when {
      mockCredentialsSuppler.readCredentialsFor(testClientKey)
    } thenReturn Optional.of[AppMarketCredentials](testAppmarketCredentials)

    when {
      mockAuthorizationTokenGenerator.generateAuthorizationHeaderValue(
        "GET", testEventUrl, testAppmarketCredentials
      )
    } thenReturn "afscgg"

    httpServerMock
      .givenThat {
        get(urlEqualTo(testHttpResource)) willReturn {
          aResponse withBody expectedEventPayloadJson
        }
      }

    val expectedEvent = Event(
      `type` = SUBSCRIPTION_NOTICE,
      marketplace = Marketplace(
        partner = "APPDIRECT",
        baseUrl = "http://sample.appdirect.com"
      ),
      creator = User(),
      payload = Payload(
        company = Company(),
        account = Some(
          Account(None)
        ),
        notice = Some(Notice(NoticeType.CLOSED))
      )
    )

    //When
    val (parsedEventId, parsedEvent) = tested.fetchRawAppMarketEvent(testAppmarketCredentials, testEventUrl)

    //Then
    parsedEvent shouldEqual expectedEvent
    parsedEventId shouldEqual testEventId
  }
  //TODO: Add tests to parse all new event types !!
  
}
