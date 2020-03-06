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
class CdrsController(private val requestHandlerBuilder: RequestHandlerBuilder) {


    /**
     * SENDER INTERFACE
     */

    @GetMapping("/scpi/sender/2.2/cdrs")
    fun getCdrsFromDataOwner(@RequestHeader("authorization") authorization: String,
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
                             @RequestParam("limit", required = false) limit: Int?): ResponseEntity<ScpiResponse<Array<CDR>>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val params = mapOf("date_from" to dateFrom, "date_to" to dateTo, "offset" to offset, "limit" to limit).filterNull()

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CDRS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlEncodedParams = params)

        val request: RequestHandler<Array<CDR>> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponseWithPaginationHeaders()

    }

    @GetMapping("/scpi/sender/2.2/cdrs/page/{uid}")
    fun getCdrPageFromDataOwner(@RequestHeader("authorization") authorization: String,
                                @RequestHeader("SCN-Signature") signature: String? = null,
                                @RequestHeader("X-Request-ID") requestID: String,
                                @RequestHeader("X-Correlation-ID") correlationID: String,
                                @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                @PathVariable uid: String): ResponseEntity<ScpiResponse<Array<CDR>>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CDRS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = uid)

        val request: RequestHandler<Array<CDR>> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest(proxied = true)
                .getResponseWithPaginationHeaders()
    }


    /**
     * RECEIVER INTERFACE
     */

    @GetMapping("/scpi/receiver/2.2/cdrs/{cdrID}")
    fun getClientOwnedCdr(@RequestHeader("authorization") authorization: String,
                          @RequestHeader("SCN-Signature") signature: String? = null,
                          @RequestHeader("X-Request-ID") requestID: String,
                          @RequestHeader("X-Correlation-ID") correlationID: String,
                          @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                          @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                          @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                          @RequestHeader("SCPI-to-party-id") toPartyID: String,
                          @PathVariable cdrID: String): ResponseEntity<ScpiResponse<CDR>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CDRS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = cdrID)

        val request: RequestHandler<CDR> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest(proxied = true)
                .getResponse()
    }

//    @Transactional
    @PostMapping("/scpi/receiver/2.2/cdrs")
    fun postClientOwnedCdr(@RequestHeader("authorization") authorization: String,
                           @RequestHeader("SCN-Signature") signature: String? = null,
                           @RequestHeader("X-Request-ID") requestID: String,
                           @RequestHeader("X-Correlation-ID") correlationID: String,
                           @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                           @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                           @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                           @RequestHeader("SCPI-to-party-id") toPartyID: String,
                           @RequestBody body: CDR): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CDRS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.POST,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponseWithLocationHeader("/scpi/receiver/2.2/cdrs")
    }

}
