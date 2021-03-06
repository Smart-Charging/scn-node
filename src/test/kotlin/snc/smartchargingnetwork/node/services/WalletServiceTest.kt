package snc.smartchargingnetwork.node.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.tuples.generated.Tuple2
import snc.smartchargingnetwork.node.models.exceptions.ScpiHubConnectionProblemException
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.tools.generatePrivateKey
import snc.openchargingnetwork.contracts.Registry
import snc.smartchargingnetwork.node.config.NodeProperties
import snc.smartchargingnetwork.node.models.ScnHeaders

class WalletServiceTest {

    private val privateKey = "71eb44ccb94e00b6d462232618085daeea239daf76804acf040a7f037549598f"
    private val address = "0xF686dd2b1Cbf4c77079Ca48D176e157ddB744eeF"

    private val body = ScpiRequestVariables(
            method = HttpMethod.GET,
            module = ModuleID.SESSIONS,
            interfaceRole = InterfaceRole.RECEIVER,
            headers = ScnHeaders(
                    authorization = "Token token-c",
                    requestID = "1",
                    correlationID = "1",
                    sender = BasicRole("XXX", "DE"),
                    receiver = BasicRole("AAA", "DE")))

    private val properties: NodeProperties = mockk()
    private val registry: Registry = mockk()

    private val walletService: WalletService

    init {
        walletService = WalletService(properties, registry)
        every { properties.privateKey } returns privateKey
    }


    @Test
    fun toByteArray() {
        val sig = "0x9955af11969a2d2a7f860cb00e6a00cfa7c581f5df2dbe8ea16700b33f4b4b9" +
                "b69f945012f7ea7d3febf11eb1b78e1adc2d1c14c2cf48b25000938cc1860c83e01"
        val (r, s, v) = walletService.toByteArray(sig)
        assertThat(r.size).isEqualTo(32)
        assertThat(s.size).isEqualTo(32)
        assertThat(v.size).isEqualTo(1)
        Sign.SignatureData(v, r, s)
    }


    @Test
    fun sign() {
        val jsonStringBody = jacksonObjectMapper().writeValueAsString(body)
        val sig = walletService.sign(jsonStringBody)
        assertThat(sig.length).isEqualTo(130)
    }

    @Test
    fun `verifyRequest silently succeeds`() {
        val jsonStringBody = jacksonObjectMapper().writeValueAsString(body)
        val sig = walletService.sign(jsonStringBody)
        every { registry.getOperatorByScpi("DE".toByteArray(), "XXX".toByteArray()).sendAsync().get() } returns Tuple2(address, "")
        walletService.verify(jsonStringBody, sig, BasicRole("XXX", "DE"))
    }

    @Test
    fun `verifyRequest loudly fails`() {
        val credentials2 = Credentials.create(generatePrivateKey())
        val jsonStringBody = jacksonObjectMapper().writeValueAsString(body)
        val sig = walletService.sign(jsonStringBody)
        every { registry.getOperatorByScpi("DE".toByteArray(), "XXX".toByteArray()).sendAsync().get() } returns Tuple2(credentials2.address, "")
        try {
            walletService.verify(jsonStringBody, sig, BasicRole("XXX", "DE"))
        } catch (e: ScpiHubConnectionProblemException) {
            assertThat(e.message).isEqualTo("Could not verify SCN-Signature of request")
        }
    }


}
