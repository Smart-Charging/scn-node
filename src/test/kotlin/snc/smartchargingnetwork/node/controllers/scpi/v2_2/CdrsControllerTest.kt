package snc.smartchargingnetwork.node.controllers.scpi.v2_2

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import snc.smartchargingnetwork.node.data.exampleCDR
import snc.smartchargingnetwork.node.tools.generateUUIDv4Token
import org.hamcrest.Matchers.hasSize
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import snc.smartchargingnetwork.node.models.*
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.services.RequestHandler
import snc.smartchargingnetwork.node.services.RequestHandlerBuilder


@WebMvcTest(CdrsController::class)
class CdrsControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var requestHandlerBuilder: RequestHandlerBuilder


    @Test
    fun `When GET sender CDRs should return paginated response`() {

        val sender = BasicRole("EMY", "DE")
        val receiver = BasicRole("ZTP", "CH")

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CDRS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(
                        authorization = "Token token-c",
                        requestID = generateUUIDv4Token(),
                        correlationID = generateUUIDv4Token(),
                        sender = sender,
                        receiver = receiver),
                urlEncodedParams = mapOf("limit" to 100))

        val mockRequestHandler = mockk<RequestHandler<Array<CDR>>>()

        val responseHeaders = HttpHeaders()
        responseHeaders["Link"] = "https://node.scn.co/scpi/sender/2.2/cdrs/page/43; rel=\"next\""
        responseHeaders["X-Limit"] = "100"

        every { requestHandlerBuilder.build<Array<CDR>>(requestVariables) } returns mockRequestHandler

        every { mockRequestHandler.validateSender().forwardRequest().getResponseWithPaginationHeaders() } returns ResponseEntity
                .status(200)
                .headers(responseHeaders)
                .body(ScpiResponse(statusCode = 1000, data = arrayOf(exampleCDR)))

        mockMvc.perform(get("/scpi/sender/2.2/cdrs")
                .header("Authorization", "Token token-c")
                .header("X-Request-ID", requestVariables.headers.requestID)
                .header("X-Correlation-ID", requestVariables.headers.correlationID)
                .header("SCPI-from-country-code", requestVariables.headers.sender.country)
                .header("SCPI-from-party-id", requestVariables.headers.sender.id)
                .header("SCPI-to-country-code", requestVariables.headers.receiver.country)
                .header("SCPI-to-party-id", requestVariables.headers.receiver.id)
                .param("limit", "100"))
                .andExpect(status().isOk)
                .andExpect(header().string("Link", "https://node.scn.co/scpi/sender/2.2/cdrs/page/43; rel=\"next\""))
                .andExpect(header().string("X-Limit", "100"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("\$.status_code").value(1000))
                .andExpect(jsonPath("\$.status_message").doesNotExist())
                .andExpect(jsonPath("\$.data").isArray)
                .andExpect(jsonPath("\$.data", hasSize<Array<CDR>>(1)))
                .andExpect(jsonPath("\$.data[0].id").value(exampleCDR.id))
                .andExpect(jsonPath("\$.data[0].party_id").value(exampleCDR.partyID))
                .andExpect(jsonPath("\$.timestamp").isString)
    }

    @Test
    fun `When GET sender CDRs page should return proxied cdrs list page`() {

        val sender = BasicRole("EMY", "DE")
        val receiver = BasicRole("ZTP", "CH")

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CDRS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(
                        authorization = "Token token-c",
                        requestID = generateUUIDv4Token(),
                        correlationID = generateUUIDv4Token(),
                        sender = sender,
                        receiver = receiver),
                urlPathVariables = "67")

        val mockRequestHandler = mockk<RequestHandler<Array<CDR>>>()

        val responseHeaders = HttpHeaders()
        responseHeaders["Link"] = "https://node.scn.co/scpi/sender/2.2/cdrs/page/68; rel=\"next\""
        responseHeaders["X-Limit"] = "100"

        every { requestHandlerBuilder.build<Array<CDR>>(requestVariables) } returns mockRequestHandler

        every { mockRequestHandler.validateSender().forwardRequest(true).getResponseWithPaginationHeaders() } returns ResponseEntity
                .status(200)
                .headers(responseHeaders)
                .body(ScpiResponse(statusCode = 1000, data = arrayOf(exampleCDR)))

        mockMvc.perform(get("/scpi/sender/2.2/cdrs/page/${requestVariables.urlPathVariables}")
                .header("Authorization", "Token token-c")
                .header("X-Request-ID", requestVariables.headers.requestID)
                .header("X-Correlation-ID", requestVariables.headers.correlationID)
                .header("SCPI-from-country-code", requestVariables.headers.sender.country)
                .header("SCPI-from-party-id", requestVariables.headers.sender.id)
                .header("SCPI-to-country-code", requestVariables.headers.receiver.country)
                .header("SCPI-to-party-id", requestVariables.headers.receiver.id)
                .param("limit", "100"))
                .andExpect(status().isOk)
                .andExpect(header().string("Link", "https://node.scn.co/scpi/sender/2.2/cdrs/page/68; rel=\"next\""))
                .andExpect(header().string("X-Limit", "100"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("\$.status_code").value(1000))
                .andExpect(jsonPath("\$.status_message").doesNotExist())
                .andExpect(jsonPath("\$.data").isArray)
                .andExpect(jsonPath("\$.data", hasSize<Array<CDR>>(1)))
                .andExpect(jsonPath("\$.data[0].id").value(exampleCDR.id))
                .andExpect(jsonPath("\$.data[0].party_id").value(exampleCDR.partyID))
                .andExpect(jsonPath("\$.timestamp").isString)
    }

    @Test
    fun `When GET receiver CDRs should return single CDR`() {

        val sender = BasicRole("ZTP", "CH")
        val receiver = BasicRole("EMY", "DE")

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CDRS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(
                        authorization = "Token token-c",
                        requestID = generateUUIDv4Token(),
                        correlationID = generateUUIDv4Token(),
                        sender = sender,
                        receiver = receiver),
                urlPathVariables = "6534")

        val mockRequestHandler = mockk<RequestHandler<CDR>>()

        every { requestHandlerBuilder.build<CDR>(requestVariables) } returns mockRequestHandler

        every { mockRequestHandler.validateSender().forwardRequest(true).getResponse() } returns ResponseEntity
                .status(200)
                .body(ScpiResponse(statusCode = 1000, data = exampleCDR))

        mockMvc.perform(get("/scpi/receiver/2.2/cdrs/${requestVariables.urlPathVariables}")
                .header("Authorization", "Token token-c")
                .header("X-Request-ID", requestVariables.headers.requestID)
                .header("X-Correlation-ID", requestVariables.headers.correlationID)
                .header("SCPI-from-country-code", requestVariables.headers.sender.country)
                .header("SCPI-from-party-id", requestVariables.headers.sender.id)
                .header("SCPI-to-country-code", requestVariables.headers.receiver.country)
                .header("SCPI-to-party-id", requestVariables.headers.receiver.id))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("\$.status_code").value(1000))
                .andExpect(jsonPath("\$.status_message").doesNotExist())
                .andExpect(jsonPath("\$.data.id").value(exampleCDR.id))
                .andExpect(jsonPath("\$.data.party_id").value(exampleCDR.partyID))
                .andExpect(jsonPath("\$.timestamp").isString)
    }

    @Test
    fun `when POST receiver cdrs should return Location header`() {

        val sender = BasicRole("ZTP", "CH")
        val receiver = BasicRole("EMY", "DE")

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CDRS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.POST,
                headers = ScnHeaders(
                        authorization = "Token token-c",
                        requestID = generateUUIDv4Token(),
                        correlationID = generateUUIDv4Token(),
                        sender = sender,
                        receiver = receiver),
                body = exampleCDR)

        val mockRequestHandler = mockk<RequestHandler<Unit>>()

        val responseHeaders = HttpHeaders()
        responseHeaders["Location"] = "https://super.hub.net/scpi/receiver/2.2/cdrs/6545"

        every { requestHandlerBuilder.build<Unit>(requestVariables) } returns mockRequestHandler

        every { mockRequestHandler.validateSender().forwardRequest().getResponseWithLocationHeader("/scpi/receiver/2.2/cdrs") } returns ResponseEntity
                .status(200)
                .headers(responseHeaders)
                .body(ScpiResponse(statusCode = 1000))

        mockMvc.perform(post("/scpi/receiver/2.2/cdrs")
                .header("Authorization", "Token token-c")
                .header("X-Request-ID", requestVariables.headers.requestID)
                .header("X-Correlation-ID", requestVariables.headers.correlationID)
                .header("SCPI-from-country-code", requestVariables.headers.sender.country)
                .header("SCPI-from-party-id", requestVariables.headers.sender.id)
                .header("SCPI-to-country-code", requestVariables.headers.receiver.country)
                .header("SCPI-to-party-id", requestVariables.headers.receiver.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(exampleCDR)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("\$.status_code").value(1000))
                .andExpect(jsonPath("\$.status_message").doesNotExist())
                .andExpect(jsonPath("\$.data").doesNotExist())
                .andExpect(jsonPath("\$.timestamp").isString)
                .andExpect(header().string("Location", "https://super.hub.net/scpi/receiver/2.2/cdrs/6545"))
    }
}
