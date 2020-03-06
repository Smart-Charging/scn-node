package snc.smartchargingnetwork.node.controllers.scpi

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import snc.smartchargingnetwork.node.repositories.PlatformRepository
import snc.smartchargingnetwork.node.config.NodeProperties
import snc.smartchargingnetwork.node.models.scpi.ScpiStatus
import snc.smartchargingnetwork.node.models.entities.PlatformEntity
import snc.smartchargingnetwork.node.models.scpi.Endpoint

@WebMvcTest(VersionsController::class)
class VersionsControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var repository: PlatformRepository

    @MockkBean
    lateinit var properties: NodeProperties

    @Test
    fun `When GET versions then return version information`() {
        val platform = PlatformEntity()
        every { repository.existsByAuth_TokenA(platform.auth.tokenA) } returns true
        every { properties.url } returns "http://localhost:8070"
        mockMvc.perform(get("/scpi/versions")
                .header("Authorization", "Token ${platform.auth.tokenA}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("\$.status_code").value(ScpiStatus.SUCCESS.code))
                .andExpect(jsonPath("\$.status_message").doesNotExist())
                .andExpect(jsonPath("\$.timestamp").isString)
                .andExpect(jsonPath("\$.data").isArray)
                .andExpect(jsonPath("\$.data[0]").isMap)
                .andExpect(jsonPath("\$.data[0].version").value("2.2"))
                .andExpect(jsonPath("\$.data[0].url").value("http://localhost:8070/scpi/2.2"))
    }

    @Test
    fun `When GET 2_2 then return version details`() {
        val platform = PlatformEntity()
        every { repository.existsByAuth_TokenA(platform.auth.tokenA) } returns true
        every { properties.url } returns "https://broker.provider.com"
        mockMvc.perform(get("/scpi/2.2")
                .header("Authorization", "Token ${platform.auth.tokenA}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("\$.status_code").value(ScpiStatus.SUCCESS.code))
                .andExpect(jsonPath("\$.status_message").doesNotExist())
                .andExpect(jsonPath("\$.timestamp").isString)
                .andExpect(jsonPath("\$.data.version").value("2.2"))
                .andExpect(jsonPath("\$.data.endpoints", hasSize<Array<Endpoint>>(16)))
                .andExpect(jsonPath("\$.data.endpoints[0].identifier").value("cdrs"))
                .andExpect(jsonPath("\$.data.endpoints[0].role").value("SENDER"))
                .andExpect(jsonPath("\$.data.endpoints[0].url").value("https://broker.provider.com/scpi/sender/2.2/cdrs"))
                .andExpect(jsonPath("\$.data.endpoints[1].identifier").value("cdrs"))
                .andExpect(jsonPath("\$.data.endpoints[1].role").value("RECEIVER"))
                .andExpect(jsonPath("\$.data.endpoints[1].url").value("https://broker.provider.com/scpi/receiver/2.2/cdrs"))
    }

}
