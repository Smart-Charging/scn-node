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
import snc.smartchargingnetwork.node.models.ScnHeaders
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.services.RequestHandler
import snc.smartchargingnetwork.node.services.RequestHandlerBuilder


@RestController
class ChargingProfilesController(private val requestHandlerBuilder: RequestHandlerBuilder) {

    /**
     * SENDER INTERFACE
     */

    @PostMapping("/scpi/2.2/sender/chargingprofiles/result/{uid}")
    fun postGenericChargingProfileResult(@RequestHeader("Authorization") authorization: String,
                                         @RequestHeader("SCN-Signature") signature: String? = null,
                                         @RequestHeader("X-Request-ID") requestID: String,
                                         @RequestHeader("X-Correlation-ID") correlationID: String,
                                         @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                         @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                         @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                         @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                         @PathVariable uid: String,
                                         @RequestBody body: GenericChargingProfileResult): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CHARGING_PROFILES,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.POST,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = uid,
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest(proxied = true)
                .getResponse()
    }

    @PutMapping("/scpi/2.2/sender/chargingprofiles/{sessionId}")
    fun putSenderChargingProfile(@RequestHeader("Authorization") authorization: String,
                                 @RequestHeader("SCN-Signature") signature: String? = null,
                                 @RequestHeader("X-Request-ID") requestID: String,
                                 @RequestHeader("X-Correlation-ID") correlationID: String,
                                 @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                 @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                 @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                 @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                 @PathVariable sessionId: String,
                                 @RequestBody body: ActiveChargingProfile): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CHARGING_PROFILES,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = sessionId,
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }


    /**
     * RECEIVER INTERFACE
     */

    @GetMapping("/scpi/2.2/receiver/chargingprofiles/{sessionId}")
    fun getReceiverChargingProfile(@RequestHeader("Authorization") authorization: String,
                                   @RequestHeader("SCN-Signature") signature: String? = null,
                                   @RequestHeader("X-Request-ID") requestID: String,
                                   @RequestHeader("X-Correlation-ID") correlationID: String,
                                   @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                   @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                   @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                   @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                   @PathVariable sessionId: String,
                                   @RequestParam duration: Int,
                                   @RequestParam("response_url") responseUrl: String): ResponseEntity<ScpiResponse<ChargingProfileResponse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CHARGING_PROFILES,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = sessionId,
                urlEncodedParams = mapOf("duration" to duration, "response_url" to responseUrl))

        val request: RequestHandler<ChargingProfileResponse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardModifiableRequest(responseUrl) {
                    requestVariables.copy(urlEncodedParams = mapOf("duration" to duration, "response_url" to it))
                }
                .getResponse()
    }

    @PutMapping("/scpi/2.2/receiver/chargingprofiles/{sessionId}")
    fun putReceiverChargingProfile(@RequestHeader("Authorization") authorization: String,
                                   @RequestHeader("SCN-Signature") signature: String? = null,
                                   @RequestHeader("X-Request-ID") requestID: String,
                                   @RequestHeader("X-Correlation-ID") correlationID: String,
                                   @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                   @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                   @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                   @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                   @PathVariable sessionId: String,
                                   @RequestBody body: SetChargingProfile): ResponseEntity<ScpiResponse<ChargingProfileResponse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CHARGING_PROFILES,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = sessionId,
                body = body)

        val request: RequestHandler<ChargingProfileResponse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardModifiableRequest(body.responseUrl) {
                    requestVariables.copy(body = body.copy(responseUrl = it))
                }
                .getResponse()
    }

    @DeleteMapping("/scpi/2.2/receiver/chargingprofiles/{sessionId}")
    fun deleteReceiverChargingProfile(@RequestHeader("Authorization") authorization: String,
                                      @RequestHeader("SCN-Signature") signature: String? = null,
                                      @RequestHeader("X-Request-ID") requestID: String,
                                      @RequestHeader("X-Correlation-ID") correlationID: String,
                                      @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                      @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                      @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                      @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                      @PathVariable sessionId: String,
                                      @RequestParam("response_url") responseUrl: String): ResponseEntity<ScpiResponse<ChargingProfileResponse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.CHARGING_PROFILES,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.DELETE,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = sessionId,
                urlEncodedParams = mapOf("response_url" to responseUrl))

        val request: RequestHandler<ChargingProfileResponse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardModifiableRequest(responseUrl) {
                    requestVariables.copy(urlEncodedParams = mapOf("response_url" to it))
                }
                .getResponse()
    }


}
