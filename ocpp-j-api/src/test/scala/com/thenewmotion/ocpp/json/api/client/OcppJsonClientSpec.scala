package com.thenewmotion.ocpp
package json
package api
package client

import java.net.URI
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.mutable.{After, Specification}
import org.specs2.specification.Scope
import org.mockserver.integration.ClientAndServer
import org.mockserver.mock.action.ExpectationCallback
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.HttpStatusCode.SWITCHING_PROTOCOLS_101
import org.mockserver.model.{ConnectionOptions, Header, HttpRequest}
import messages.ChargePointReq

class OcppJsonClientSpec extends Specification {
  sequential

  "OcppJsonClient" should {
    "negotiate the correct ocpp version" in {
      "when requesting ocpp1.6" in new TestScope {
        val requesting = List(Version.V16)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15V16)
        createOcppJsonClient(requesting).connection.ocppVersion must beEqualTo(Version.V16)
      }
      "when requesting ocpp1.5, ocpp1.6" in new TestScope {
        val requesting = List(Version.V15, Version.V16)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15V16)
        createOcppJsonClient(requesting).connection.ocppVersion must beEqualTo(Version.V15)
      }
      "when requesting ocpp1.6, ocpp1.5" in new TestScope {
        val requesting = List(Version.V16, Version.V15)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15V16)
        createOcppJsonClient(requesting).connection.ocppVersion must beEqualTo(Version.V16)
      }
      "when requesting ocpp1.6, ocpp1.5 and server supports 1.5 only" in new TestScope {
        val requesting = List(Version.V16, Version.V15)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15)
        createOcppJsonClient(requesting).connection.ocppVersion must beEqualTo(Version.V15)
      }
      "when requesting ocpp1.6 and server supports 1.5 only" in new TestScope {
        val requesting = List(Version.V16)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15)
        createOcppJsonClient(requesting) must throwA[SimpleClientWebSocketComponent.WebSocketErrorWhenOpeningException]
      }
      "when requesting a version the JSON client doesn't support" in new TestScope {
        val requesting = List(Version.V12)
        createOcppJsonClient(requesting) must throwA[OcppJsonClient.VersionNotSupported]
      }
      "when requesting to a server that's not listening" in new TestScope {
        val requesting = List(Version.V15)
        server.stop()
        createOcppJsonClient(requesting) must throwA[java.net.ConnectException]
      }
    }
  }

  private trait TestScope extends Scope with After {

    lazy val port = TestScope.nextPortNumber.getAndIncrement()
    lazy val server: ClientAndServer = ClientAndServer.startClientAndServer(port)

    override def after = server.stop

    val path = "/ocppws"
    val chargerId = "test-charger"
    val centralSystemUri = s"ws://localhost:$port$path"

    def createOcppJsonClient(versions: Seq[Version]) =
      OcppJsonClient(chargerId, new URI(centralSystemUri), versions) {
        (_: ChargePointReq) =>
          Future.failed(OcppException(PayloadErrorCode.NotSupported, "OcppJsonClientSpec"))
      }

    import org.mockserver.model.HttpCallback

    val V15 = new HttpCallback().withCallbackClass(
      "com.thenewmotion.ocpp.json.api.client.UpgradeRequestCallBackV15"
    )

    val V15V16 = new HttpCallback().withCallbackClass(
      "com.thenewmotion.ocpp.json.api.client.UpgradeRequestCallBackV15V16"
    )
  }

  object TestScope {
    val nextPortNumber = new AtomicInteger(1080)
  }
}

class UpgradeRequestCallBackV15 extends UpgradeRequestCallBack {
  val ocppVersions = Seq(Version.V15)
}
class UpgradeRequestCallBackV15V16 extends UpgradeRequestCallBack {
  val ocppVersions = Seq(Version.V15, Version.V16)
}
trait UpgradeRequestCallBack extends ExpectationCallback {
  def ocppVersions: Seq[Version]
  lazy val subProtocols = ocppVersions.map(SimpleClientWebSocketComponent.wsSubProtocolForOcppVersion)
  private val magicGuid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
  private val rfc2616Separators = "[()<>@,;:\\\\\"/\\[\\]?={} \t]+"
  private val mDigest: MessageDigest = MessageDigest.getInstance("SHA1")
  def handle(httpRequest: HttpRequest) = {
    val headers: Seq[Header] = {
      val webSocketKey = httpRequest.getFirstHeader("Sec-WebSocket-Key")
      val bytes = mDigest.digest((webSocketKey + magicGuid).getBytes)
      val webSocketAccept = Base64.getEncoder.encodeToString(bytes)
      Seq(
        new Header("Upgrade", "websocket"),
        new Header("Connection", "Upgrade"),
        new Header("Sec-WebSocket-Accept", webSocketAccept)
      )
    } ++ {
      val requestedProtocols = httpRequest.getFirstHeader(SimpleClientWebSocketComponent.subProtoHeader)
      val subProtocol = requestedProtocols.split(rfc2616Separators).intersect(subProtocols).headOption
      subProtocol.fold(Seq.empty[Header]) { protocol =>
        Seq(new Header(SimpleClientWebSocketComponent.subProtoHeader, protocol))
      }
    }
    response()
      .withStatusCode(SWITCHING_PROTOCOLS_101.code())
      .withHeaders(headers: _*)
      .withConnectionOptions(
        new ConnectionOptions()
          .withSuppressConnectionHeader(true)
          .withSuppressContentLengthHeader(true)
      )
  }
}
