package snc.smartchargingnetwork.node.integration.parties

import com.fasterxml.jackson.module.kotlin.readValue
import khttp.responses.Response
import org.web3j.crypto.Credentials as KeyPair
import smartcharging.openchargingnetwork.notary.Notary
import smartcharging.openchargingnetwork.notary.SignableHeaders
import smartcharging.openchargingnetwork.notary.ValuesToSign
import snc.smartchargingnetwork.node.data.exampleCDR
import snc.smartchargingnetwork.node.data.exampleLocation1
import snc.smartchargingnetwork.node.data.exampleToken
import snc.smartchargingnetwork.node.integration.objectMapper
import snc.smartchargingnetwork.node.integration.privateKey
import snc.smartchargingnetwork.node.integration.toMap
import snc.smartchargingnetwork.node.models.scpi.*

class MspServer(private val credentials: KeyPair, val party: BasicRole, port: Int): PartyServer(credentials, party, port) {

    // could be nicer to use e.g. RxJava instead
    var asyncCommandsResponse: CommandResult? = null

    init {
        app.get("/scpi/versions/2.2") {
            it.json(ScpiResponse(
                    statusCode = 1000,
                    data = VersionDetail(version = "2.2", endpoints = listOf(
                            Endpoint(
                                    identifier = "credentials",
                                    role = InterfaceRole.RECEIVER,
                                    url = urlBuilder("/scpi/msp/2.2/credentials")),
                            Endpoint(
                                    identifier = "cdrs",
                                    role = InterfaceRole.RECEIVER,
                                    url = urlBuilder("/scpi/msp/2.2/cdrs")),
                            Endpoint(
                                    identifier = "commands",
                                    role = InterfaceRole.SENDER,
                                    url = urlBuilder("/scpi/msp/2.2/commands"))))))
        }

        app.get("/scpi/msp/2.2/cdrs/1") {
            val body = ScpiResponse(statusCode = 1000, data = exampleCDR)
            val valuesToSign = ValuesToSign(body = body)
            body.signature = Notary().sign(valuesToSign, credentials.privateKey()).serialize()
            it.json(body)
        }

        app.post("/scpi/msp/2.2/cdrs") {
            val headers = SignableHeaders(location = urlBuilder("/scpi/msp/2.2/cdrs/1"))
            val body = ScpiResponse(statusCode = 1000, data = null)
            val valuesToSign = ValuesToSign(headers = headers, body = body)
            body.signature = Notary().sign(valuesToSign, credentials.privateKey()).serialize()
            it.header("location", headers.location!!).json(body)
        }

        app.post("/scpi/msp/2.2/commands/START_SESSION/1") {
            asyncCommandsResponse = it.body<CommandResult>()
            val body = ScpiResponse(statusCode = 1000, data = null)
            val valuesToSign = ValuesToSign(body = body)
            body.signature = Notary().sign(valuesToSign, credentials.privateKey()).serialize()
            it.json(body)
        }
    }

    fun getLocation(to: BasicRole): Response {
        val headers = getSignableHeaders(to)
        val request = ValuesToSign(headers = headers, body = null)
        val signature = Notary().sign(request, credentials.privateKey()).serialize()
        return khttp.get("$node/scpi/sender/2.2/locations/1", headers = headers.toMap(tokenC, signature))
    }

    fun getLocationList(to: BasicRole): Response {
        val headers = getSignableHeaders(to)
        val params = mapOf("limit" to "4")
        val request = ValuesToSign(headers = headers, params = params, body = null)
        val signature = Notary().sign(request, credentials.privateKey()).serialize()
        return khttp.get("$node/scpi/sender/2.2/locations", params=params, headers = headers.toMap(tokenC, signature))
    }

    fun getNextLink(to: BasicRole, next: String): Response {
        val headers = getSignableHeaders(to)
        val request = ValuesToSign(headers = headers, body = null)
        val signature = Notary().sign(request, credentials.privateKey()).serialize()
        return khttp.get(next, headers = headers.toMap(tokenC, signature))
    }

    fun sendStartSession(to: BasicRole): Response {
        val body = StartSession(
                responseURL = urlBuilder("/scpi/msp/2.2/commands/START_SESSION/1"),
                token = exampleToken,
                locationID = exampleLocation1.id,
                evseUID = exampleLocation1.evses!![0].uid)
        val headers = getSignableHeaders(to)
        val request = ValuesToSign(headers = headers, body = body)
        val signature = Notary().sign(request, credentials.privateKey()).serialize()
        val json: Map<String, Any?> = objectMapper.readValue(objectMapper.writeValueAsString(body))
        return khttp.post("$node/scpi/receiver/2.2/commands/START_SESSION", headers = headers.toMap(tokenC, signature), json = json)
    }

}
