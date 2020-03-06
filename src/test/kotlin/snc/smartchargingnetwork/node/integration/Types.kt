package snc.smartchargingnetwork.node.integration

import snc.smartchargingnetwork.node.integration.parties.CpoServer
import snc.smartchargingnetwork.node.models.scpi.BasicRole

class JavalinException(val httpCode: Int = 200, val scpiCode: Int = 2001, message: String): Exception(message)

data class CpoTestCase(val party: BasicRole, val address: String, val operator: String, val server: CpoServer)
