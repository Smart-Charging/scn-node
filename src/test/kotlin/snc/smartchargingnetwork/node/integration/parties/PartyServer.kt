package snc.smartchargingnetwork.node.integration.parties

import io.javalin.Javalin
import org.web3j.crypto.Credentials as KeyPair
import smartcharging.openchargingnetwork.notary.SignableHeaders
import snc.smartchargingnetwork.node.integration.JavalinException
import snc.smartchargingnetwork.node.integration.coerceToJson
import snc.smartchargingnetwork.node.integration.getRegistryInstance
import snc.smartchargingnetwork.node.integration.getTokenA
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.tools.generateUUIDv4Token

open class PartyServer(private val credentials: KeyPair, private val party: BasicRole, private val port: Int) {

    val app: Javalin = Javalin.create().start(port)
    private val tokenB: String = generateUUIDv4Token()
    lateinit var tokenC: String
    lateinit var node: String

    init {
        app.exception(JavalinException::class.java) { e, ctx ->
            ctx.status(e.httpCode).json(ScpiResponse<Unit>(statusCode = e.scpiCode, statusMessage = e.message))
        }

        app.before {
            if (it.header("Authorization") != "Token $tokenB") {
                throw JavalinException(message = "Unauthorized")
            }
        }

        app.get("/scpi/versions") {
            it.json(ScpiResponse(
                    statusCode = 1000,
                    data = listOf(Version(version = "2.2", url = urlBuilder("/scpi/versions/2.2")))
            ))
        }
    }

    fun setPartyInRegistry(registryAddress: String, operator: String) {
        val registry = getRegistryInstance(credentials, registryAddress)
        registry.setParty(party.country.toByteArray(), party.id.toByteArray(), listOf(0.toBigInteger()), operator).sendAsync().get()
        node = registry.getNode(operator).sendAsync().get()
    }

    fun registerCredentials() {
        val tokenA = getTokenA(node, listOf(party))
        val response = khttp.post("$node/scpi/2.2/credentials",
                headers = mapOf("Authorization" to "Token $tokenA"),
                json = coerceToJson(Credentials(
                        token = tokenB,
                        url = urlBuilder("/scpi/versions"),
                        roles = listOf(CredentialsRole(
                                role = Role.CPO,
                                businessDetails = BusinessDetails(name = "Some CPO"),
                                countryCode = party.country,
                                partyID = party.id)))))
        tokenC = response.jsonObject.getJSONObject("data").getString("token")
    }

    fun urlBuilder(path: String): String {
        return "http://localhost:$port$path"
    }

    fun getSignableHeaders(to: BasicRole): SignableHeaders {
        return SignableHeaders(
                correlationId = generateUUIDv4Token(),
                fromCountryCode = party.country,
                fromPartyId = party.id,
                toCountryCode = to.country,
                toPartyId = to.id)
    }

}
