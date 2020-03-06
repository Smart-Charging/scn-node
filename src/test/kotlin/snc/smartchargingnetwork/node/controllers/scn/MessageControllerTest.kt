package snc.smartchargingnetwork.node.controllers.scn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import snc.smartchargingnetwork.node.data.exampleLocation2
import snc.smartchargingnetwork.node.models.*
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.services.RequestHandler
import snc.smartchargingnetwork.node.services.RequestHandlerBuilder

@WebMvcTest(MessageController::class)
class MessageControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var requestHandlerBuilder: RequestHandlerBuilder

    @Test
    fun `When POST SCN message should forward the request to local recipient and return their SCPI response`() {

        val senderRole = BasicRole("ABC", "DE")
        val receiverRole = BasicRole("XYZ", "NL")

        val requestVariables = ScpiRequestVariables(
                method = HttpMethod.GET,
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.SENDER,
                urlPathVariables = "LOC2",
                headers = ScnHeaders(
                        authorization = "",
                        requestID = "123",
                        correlationID = "abc-123",
                        sender = senderRole,
                        receiver = receiverRole))

        val requestVariablesString = jacksonObjectMapper().writeValueAsString(requestVariables)

        val mockkRequestHandler = mockk<RequestHandler<Location>>()

        every { requestHandlerBuilder.build<Location>(requestVariablesString) } returns mockkRequestHandler

        every {
            mockkRequestHandler
                    .validateScnMessage("0x1234")
                    .forwardRequest()
                    .getResponseWithAllHeaders()
        } returns ResponseEntity
                .status(200)
                .body(ScpiResponse(1000, data = exampleLocation2))

        mockMvc.perform(post("/scn/message")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", "xyz")
                .header("SCN-Signature", "0x1234")
                .content(jacksonObjectMapper().writeValueAsString(requestVariables)))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("\$.status_code").value(ScpiStatus.SUCCESS.code))
                .andExpect(jsonPath("\$.status_message").doesNotExist())
                .andExpect(jsonPath("\$.timestamp").isString)
                .andExpect(jsonPath("\$.data.id").value(exampleLocation2.id))
    }
}
