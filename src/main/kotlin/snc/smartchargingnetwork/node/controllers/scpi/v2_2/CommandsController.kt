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
import snc.smartchargingnetwork.node.config.NodeProperties
import snc.smartchargingnetwork.node.models.*
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.services.RequestHandler
import snc.smartchargingnetwork.node.services.RequestHandlerBuilder

@RestController
class CommandsController(private val requestHandlerBuilder: RequestHandlerBuilder,
                         private val properties: NodeProperties) {


    /**
     * SENDER INTERFACE
     */

    @PostMapping("/scpi/sender/2.2/commands/{command}/{uid}")
    fun postAsyncResponse(@RequestHeader("authorization") authorization: String,
                          @RequestHeader("SCN-Signature") signature: String? = null,
                          @RequestHeader("X-Request-ID") requestID: String,
                          @RequestHeader("X-Correlation-ID") correlationID: String,
                          @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                          @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                          @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                          @RequestHeader("SCPI-to-party-id") toPartyID: String,
                          @PathVariable("command") command: CommandType,
                          @PathVariable("uid") uid: String,
                          @RequestBody body: CommandResult): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.COMMANDS,
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


    /**
     * RECEIVER INTERFACE
     */

//    @Transactional
    @PostMapping("/scpi/receiver/2.2/commands/CANCEL_RESERVATION")
    fun postCancelReservation(@RequestHeader("authorization") authorization: String,
                              @RequestHeader("SCN-Signature") signature: String? = null,
                              @RequestHeader("X-Request-ID") requestID: String,
                              @RequestHeader("X-Correlation-ID") correlationID: String,
                              @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                              @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                              @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                              @RequestHeader("SCPI-to-party-id") toPartyID: String,
                              @RequestBody body: CancelReservation): ResponseEntity<ScpiResponse<CommandResponse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.COMMANDS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.POST,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "CANCEL_RESERVATION",
                body = body)

        val request: RequestHandler<CommandResponse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardModifiableRequest(body.responseURL) { requestVariables.copy(body = body.copy(responseURL = it)) }
                .getResponse()
    }


    @PostMapping("/scpi/receiver/2.2/commands/RESERVE_NOW")
    fun postReserveNow(@RequestHeader("authorization") authorization: String,
                       @RequestHeader("SCN-Signature") signature: String? = null,
                       @RequestHeader("X-Request-ID") requestID: String,
                       @RequestHeader("X-Correlation-ID") correlationID: String,
                       @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                       @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                       @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                       @RequestHeader("SCPI-to-party-id") toPartyID: String,
                       @RequestBody body: ReserveNow): ResponseEntity<ScpiResponse<CommandResponse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.COMMANDS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.POST,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "RESERVE_NOW",
                body = body)

        val request: RequestHandler<CommandResponse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardModifiableRequest(body.responseURL) { requestVariables.copy(body = body.copy(responseURL = it)) }
                .getResponse()
    }


    @PostMapping("/scpi/receiver/2.2/commands/START_SESSION")
    fun postStartSession(@RequestHeader("authorization") authorization: String,
                         @RequestHeader("SCN-Signature") signature: String? = null,
                         @RequestHeader("X-Request-ID") requestID: String,
                         @RequestHeader("X-Correlation-ID") correlationID: String,
                         @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                         @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                         @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                         @RequestHeader("SCPI-to-party-id") toPartyID: String,
                         @RequestBody body: StartSession): ResponseEntity<ScpiResponse<CommandResponse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.COMMANDS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.POST,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "START_SESSION",
                body = body)

        val request: RequestHandler<CommandResponse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardModifiableRequest(body.responseURL) { requestVariables.copy(body = body.copy(responseURL = it)) }
                .getResponse()
    }


    @PostMapping("/scpi/receiver/2.2/commands/STOP_SESSION")
    fun postStopSession(@RequestHeader("authorization") authorization: String,
                        @RequestHeader("SCN-Signature") signature: String? = null,
                        @RequestHeader("X-Request-ID") requestID: String,
                        @RequestHeader("X-Correlation-ID") correlationID: String,
                        @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                        @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                        @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                        @RequestHeader("SCPI-to-party-id") toPartyID: String,
                        @RequestBody body: StopSession): ResponseEntity<ScpiResponse<CommandResponse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.COMMANDS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.POST,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "STOP_SESSION",
                body = body)

        val request: RequestHandler<CommandResponse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardModifiableRequest(body.responseURL) { requestVariables.copy(body = body.copy(responseURL = it)) }
                .getResponse()
    }


    @PostMapping("/scpi/receiver/2.2/commands/UNLOCK_CONNECTOR")
    fun postUnlockConnector(@RequestHeader("authorization") authorization: String,
                            @RequestHeader("SCN-Signature") signature: String? = null,
                            @RequestHeader("X-Request-ID") requestID: String,
                            @RequestHeader("X-Correlation-ID") correlationID: String,
                            @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                            @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                            @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                            @RequestHeader("SCPI-to-party-id") toPartyID: String,
                            @RequestBody body: UnlockConnector): ResponseEntity<ScpiResponse<CommandResponse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.COMMANDS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.POST,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "UNLOCK_CONNECTOR",
                body = body)

        val request: RequestHandler<CommandResponse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardModifiableRequest(body.responseURL) { requestVariables.copy(body = body.copy(responseURL = it)) }
                .getResponse()
    }

}
