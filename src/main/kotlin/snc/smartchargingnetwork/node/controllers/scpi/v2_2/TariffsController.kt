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

package snc.smartchargingnetwork.node.controllers.scpi.v2_2

import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import snc.smartchargingnetwork.node.models.*
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.services.RequestHandler
import snc.smartchargingnetwork.node.services.RequestHandlerBuilder
import snc.smartchargingnetwork.node.tools.filterNull


@RestController
class TariffsController(private val requestHandlerBuilder: RequestHandlerBuilder) {


    /**
     * SENDER INTERFACE
     */

    @GetMapping("/scpi/sender/2.2/tariffs")
    fun getTariffsFromDataOwner(@RequestHeader("authorization") authorization: String,
                                @RequestHeader("SCN-Signature") signature: String? = null,
                                @RequestHeader("X-Request-ID") requestID: String,
                                @RequestHeader("X-Correlation-ID") correlationID: String,
                                @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                @RequestParam("date_from", required = false) dateFrom: String?,
                                @RequestParam("date_to", required = false) dateTo: String?,
                                @RequestParam("offset", required = false) offset: Int?,
                                @RequestParam("limit", required = false) limit: Int?): ResponseEntity<ScpiResponse<Array<Tariff>>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val params = mapOf("date_from" to dateFrom, "date_to" to dateTo, "offset" to offset, "limit" to limit).filterNull()

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.TARIFFS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlEncodedParams = params)

        val request: RequestHandler<Array<Tariff>> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponseWithPaginationHeaders()
    }

    @GetMapping("/scpi/sender/2.2/tariffs/page/{uid}")
    fun getTariffsPageFromDataOwner(@RequestHeader("authorization") authorization: String,
                                    @RequestHeader("SCN-Signature") signature: String? = null,
                                    @RequestHeader("X-Request-ID") requestID: String,
                                    @RequestHeader("X-Correlation-ID") correlationID: String,
                                    @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                    @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                    @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                    @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                    @PathVariable uid: String): ResponseEntity<ScpiResponse<Array<Tariff>>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.TARIFFS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = uid)

        val request: RequestHandler<Array<Tariff>> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponseWithPaginationHeaders()
    }


    /**
     * RECEIVER INTERFACE
     */

    @GetMapping("/scpi/receiver/2.2/tariffs/{countryCode}/{partyID}/{tariffID}")
    fun getClientOwnedTariff(@RequestHeader("authorization") authorization: String,
                             @RequestHeader("SCN-Signature") signature: String? = null,
                             @RequestHeader("X-Request-ID") requestID: String,
                             @RequestHeader("X-Correlation-ID") correlationID: String,
                             @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                             @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                             @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                             @RequestHeader("SCPI-to-party-id") toPartyID: String,
                             @PathVariable countryCode: String,
                             @PathVariable partyID: String,
                             @PathVariable tariffID: String): ResponseEntity<ScpiResponse<Tariff>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.TARIFFS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$tariffID")

        val request: RequestHandler<Tariff> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }


    @PutMapping("/scpi/receiver/2.2/tariffs/{countryCode}/{partyID}/{tariffID}")
    fun putClientOwnedTariff(@RequestHeader("authorization") authorization: String,
                             @RequestHeader("SCN-Signature") signature: String? = null,
                             @RequestHeader("X-Request-ID") requestID: String,
                             @RequestHeader("X-Correlation-ID") correlationID: String,
                             @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                             @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                             @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                             @RequestHeader("SCPI-to-party-id") toPartyID: String,
                             @PathVariable countryCode: String,
                             @PathVariable partyID: String,
                             @PathVariable tariffID: String,
                             @RequestBody body: Tariff): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.TARIFFS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$tariffID",
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }


    @DeleteMapping("/scpi/receiver/2.2/tariffs/{countryCode}/{partyID}/{tariffID}")
    fun deleteClientOwnedTariff(@RequestHeader("authorization") authorization: String,
                                @RequestHeader("SCN-Signature") signature: String? = null,
                                @RequestHeader("X-Request-ID") requestID: String,
                                @RequestHeader("X-Correlation-ID") correlationID: String,
                                @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                @PathVariable countryCode: String,
                                @PathVariable partyID: String,
                                @PathVariable tariffID: String): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.TARIFFS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.DELETE,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$tariffID")

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

}
