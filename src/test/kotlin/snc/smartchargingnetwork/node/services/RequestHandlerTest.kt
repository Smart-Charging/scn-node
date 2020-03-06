package snc.smartchargingnetwork.node.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import smartcharging.openchargingnetwork.notary.Notary
import snc.smartchargingnetwork.node.config.NodeProperties
import snc.smartchargingnetwork.node.data.exampleLocation1
import snc.smartchargingnetwork.node.models.*
import snc.smartchargingnetwork.node.models.entities.ScnRules
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.tools.generatePrivateKey

class RequestHandlerTest {

    private val routingService: RoutingService = mockk()
    private val httpService: HttpService = mockk()
    private val walletService: WalletService = mockk()
    private val properties: NodeProperties = mockk()

    private val requestHandlerBuilder = RequestHandlerBuilder(routingService, httpService, walletService, properties)

    @Test
    fun validateSender() {
        val variables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(
                        authorization = "",
                        requestID = "123",
                        correlationID = "456",
                        sender = BasicRole("ABC", "DE"),
                        receiver = BasicRole("XYZ", "DE")))

        every { routingService.validateSender(variables.headers.authorization, variables.headers.sender) } just Runs
        val requestHandler = requestHandlerBuilder.build<Unit>(variables)
        assertDoesNotThrow { requestHandler.validateSender() }
    }

    @Test
    fun validateScnMessage() {
        val signature = "0x12345"

        val variables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(
                        authorization = "",
                        requestID = "123",
                        correlationID = "456",
                        sender = BasicRole("ABC", "DE"),
                        receiver = BasicRole("XYZ", "DE")))

        val requestHandler = requestHandlerBuilder.build<Location>(variables)

        val variablesString = jacksonObjectMapper().writeValueAsString(variables)

        every { routingService.isRoleKnownOnNetwork(variables.headers.sender, false) } returns true
        every { routingService.isRoleKnown(variables.headers.receiver) } returns true
        every { httpService.mapper.writeValueAsString(variables) } returns variablesString
        every { walletService.verify(variablesString, signature, variables.headers.sender) } just Runs
        every { properties.signatures } returns false

        assertDoesNotThrow { requestHandler.validateScnMessage(signature) }
    }

    @Test
    fun forwardRequest_local() {
        val variables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(
                        authorization = "",
                        requestID = "123",
                        correlationID = "456",
                        sender = BasicRole("ABC", "DE"),
                        receiver = BasicRole("XYZ", "DE")),
                body = exampleLocation1)

        val requestHandler = requestHandlerBuilder.build<Unit>(variables)

        val recipientUrl = "https://msp.com/scpi/locations"
        val outgoingHeaders = ScnHeaders(
                authorization = "Token token-b",
                requestID = "666",
                correlationID = variables.headers.correlationID,
                sender = variables.headers.sender,
                receiver = variables.headers.receiver)

        val expectedResponse = HttpResponse<Unit>(
                statusCode = 200,
                headers = mapOf(),
                body = ScpiResponse(1000))

        every { routingService.validateReceiver(variables.headers.receiver) } returns Receiver.LOCAL
        every { routingService.validateWhitelisted(variables.headers.sender, variables.headers.receiver, variables.module) } just Runs
        every { properties.signatures } returns false
        every { routingService.getPlatformRules(any()) } returns ScnRules(signatures = false)
        every { routingService.prepareLocalPlatformRequest(variables, false) } returns Pair(recipientUrl, outgoingHeaders)
        every { httpService.makeScpiRequest<Unit>(recipientUrl, outgoingHeaders, variables) } returns expectedResponse

        val response = requestHandler.forwardRequest().getResponse()
        assertEquals(expectedResponse.statusCode, response.statusCodeValue)
    }

    @Test
    fun forwardRequest_local_signatureRequired() {
        val variables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(
                        authorization = "",
                        requestID = "123",
                        correlationID = "456",
                        sender = BasicRole("ABC", "DE"),
                        receiver = BasicRole("XYZ", "DE")),
                body = exampleLocation1)

        val senderKey = generatePrivateKey()
        val signature = Notary().sign(variables.toSignedValues(), senderKey)
        variables.headers.signature = signature.serialize()

        val requestHandler = requestHandlerBuilder.build<Unit>(variables)

        val recipientUrl = "https://msp.com/scpi/locations"
        val outgoingHeaders = ScnHeaders(
                authorization = "Token token-b",
                requestID = "666",
                correlationID = variables.headers.correlationID,
                sender = variables.headers.sender,
                receiver = variables.headers.receiver)

        val expectedResponse = HttpResponse<Unit>(
                statusCode = 200,
                headers = mapOf(),
                body = ScpiResponse(1000))

        val receiverKey = generatePrivateKey()
        val receiverSig = Notary().sign(expectedResponse.toSignedValues(), receiverKey)
        expectedResponse.body.signature = receiverSig.serialize()

        every { routingService.validateReceiver(variables.headers.receiver) } returns Receiver.LOCAL
        every { routingService.validateWhitelisted(variables.headers.sender, variables.headers.receiver, variables.module) } just Runs
        every { properties.signatures } returns false
        every { routingService.getPlatformRules(variables.headers.receiver) } returns ScnRules(signatures = true)
        every { routingService.getPartyDetails(variables.headers.sender) } returns RegistryPartyDetails(signature.signatory, "0x9bC1169Ca09555bf2721A5C9eC6D69c8073bfeB4")
        every { routingService.getPartyDetails(variables.headers.receiver) } returns RegistryPartyDetails(receiverSig.signatory, "0x9bC1169Ca09555bf2721A5C9eC6D69c8073bfeB4")
        every { routingService.prepareLocalPlatformRequest(variables, false) } returns Pair(recipientUrl, outgoingHeaders)
        every { httpService.makeScpiRequest<Unit>(recipientUrl, outgoingHeaders, variables) } returns expectedResponse

        val response = requestHandler.forwardRequest().getResponse()
        assertEquals(expectedResponse.statusCode, response.statusCodeValue)
    }

    @Test
    fun forwardRequest_remote() {
        val variables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(
                        authorization = "",
                        requestID = "123",
                        correlationID = "456",
                        sender = BasicRole("ABC", "DE"),
                        receiver = BasicRole("XYZ", "DE")),
                body = exampleLocation1)

        val requestHandler = requestHandlerBuilder.build<Unit>(variables)

        val recipientUrl = "https://node.scn.com/scpi/locations"
        val outgoingHeaders = ScnMessageHeaders(
                signature = "0x12345",
                requestID = "666")
        val outgoingBody = jacksonObjectMapper().writeValueAsString(variables)

        val expectedResponse = HttpResponse<Unit>(
                statusCode = 200,
                headers = mapOf(),
                body = ScpiResponse(1000))

        every { routingService.validateReceiver(variables.headers.receiver) } returns Receiver.REMOTE
        every { properties.signatures } returns false
        every { routingService.prepareRemotePlatformRequest(variables, false) } returns Triple(recipientUrl, outgoingHeaders, outgoingBody)
        every { httpService.postScnMessage<Unit>(recipientUrl, outgoingHeaders, outgoingBody) } returns expectedResponse

        val response = requestHandler.forwardRequest().getResponse()
        assertEquals(expectedResponse.statusCode, response.statusCodeValue)
    }

    @Test
    fun forwardRequest_remote_signatureRequired() {
        val variables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(
                        authorization = "",
                        requestID = "123",
                        correlationID = "456",
                        sender = BasicRole("ABC", "DE"),
                        receiver = BasicRole("XYZ", "DE")),
                body = exampleLocation1)

        val privateKey = generatePrivateKey()
        val signature = Notary().sign(variables.toSignedValues(), privateKey)
        variables.headers.signature = signature.serialize()

        val requestHandler = requestHandlerBuilder.build<Unit>(variables)

        val recipientUrl = "https://node.scn.com/scpi/locations"
        val outgoingHeaders = ScnMessageHeaders(
                signature = "0x12345",
                requestID = "666")
        val outgoingBody = jacksonObjectMapper().writeValueAsString(variables)

        val expectedResponse = HttpResponse<Unit>(
                statusCode = 200,
                headers = mapOf(),
                body = ScpiResponse(1000))

        val receiverKey = generatePrivateKey()
        val receiverSig = Notary().sign(expectedResponse.toSignedValues(), receiverKey)
        expectedResponse.body.signature = receiverSig.serialize()

        every { routingService.validateReceiver(variables.headers.receiver) } returns Receiver.REMOTE
        every { properties.signatures } returns true
        every { routingService.getPartyDetails(variables.headers.sender) } returns RegistryPartyDetails(signature.signatory, "0x7c514d15709fb091243a4dffb649361354a9b038")
        every { routingService.getPartyDetails(variables.headers.receiver) } returns RegistryPartyDetails(receiverSig.signatory, "0xd49ead20b0ae060161c9ddea9b1bc46bb29b3c58")
        every { routingService.prepareRemotePlatformRequest(variables, false) } returns Triple(recipientUrl, outgoingHeaders, outgoingBody)
        every { httpService.postScnMessage<Unit>(recipientUrl, outgoingHeaders, outgoingBody) } returns expectedResponse

        val response = requestHandler.forwardRequest().getResponse()
        assertEquals(expectedResponse.statusCode, response.statusCodeValue)
    }

    @Test
    fun validateResponse() {
        val variables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(
                        authorization = "",
                        requestID = "123",
                        correlationID = "456",
                        sender = BasicRole("ABC", "DE"),
                        receiver = BasicRole("XYZ", "DE")))

        val requestHandler = requestHandlerBuilder.build<Unit>(variables)

        assertThrows<UnsupportedOperationException> { requestHandler.getResponse() }
        assertThrows<UnsupportedOperationException> { requestHandler.getResponseWithPaginationHeaders() }
        assertThrows<UnsupportedOperationException> { requestHandler.getResponseWithLocationHeader("/proxy") }
        assertThrows<UnsupportedOperationException> { requestHandler.getResponseWithAllHeaders() }
    }

}
