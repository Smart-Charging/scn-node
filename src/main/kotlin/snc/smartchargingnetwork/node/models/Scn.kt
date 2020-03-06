/*
    Copyright 2020 Smart Charging Solutions

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/


package snc.smartchargingnetwork.node.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import smartcharging.openchargingnetwork.notary.SignableHeaders
import snc.smartchargingnetwork.node.models.scpi.BasicRole

data class ScnMessageHeaders(val requestID: String,
                             val signature: String) {

    fun toMap(): Map<String, String> {
        return mapOf(
                "X-Request-ID" to requestID,
                "SCN-Signature" to signature)
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ScnHeaders(@JsonProperty("Authorization") val authorization: String,
                      @JsonProperty("SCN-Signature") var signature: String? = null,
                      @JsonProperty("X-Request-ID") val requestID: String,
                      @JsonProperty("X-Correlation-ID") val correlationID: String,
                      val sender: BasicRole,
                      val receiver: BasicRole) {

    fun toMap(): Map<String, String?> {
        val map = mutableMapOf<String, String?>()
        map["Authorization"] = authorization
        map["SCN-Signature"] = signature
        map["X-Request-ID"] = requestID
        map["X-Correlation-ID"] = correlationID
        map["SCPI-from-country-code"] = sender.country
        map["SCPI-from-party-id"] = sender.id
        map["SCPI-to-country-code"] = receiver.country
        map["SCPI-to-party-id"] = receiver.id
        return map
    }

    fun toSignedHeaders(): SignableHeaders {
        return SignableHeaders(
                correlationId = correlationID,
                fromCountryCode = sender.country,
                fromPartyId = sender.id,
                toCountryCode = receiver.country,
                toPartyId = receiver.id)
    }

}

enum class Receiver {
    LOCAL,
    REMOTE,
}


data class ScnRules(val signatures: Boolean, val whitelist: ScnRulesList, val blacklist: ScnRulesList)
data class ScnRulesList(val active: Boolean, val list: List<ScnRulesListParty>)

enum class ScnRulesListType {
    WHITELIST,
    BLACKLIST
}

data class ScnRulesListParty(@JsonProperty("party_id") val id: String,
                             @JsonProperty("country_code") val country: String,
                             @JsonProperty("modules") val modules: List<String>)

data class RegistryPartyDetails(val address: String, val operator: String)
