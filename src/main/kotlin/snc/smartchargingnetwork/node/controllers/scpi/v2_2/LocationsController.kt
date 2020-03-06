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
class LocationsController(private val requestHandlerBuilder: RequestHandlerBuilder) {


    /**
     * SENDER INTERFACES
     */

    @GetMapping("/scpi/sender/2.2/locations")
    fun getLocationListFromDataOwner(@RequestHeader("authorization") authorization: String,
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
                                     @RequestParam("limit", required = false) limit: Int?): ResponseEntity<ScpiResponse<Array<Location>>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val params = mapOf("date_from" to dateFrom, "date_to" to dateTo, "offset" to offset, "limit" to limit).filterNull()

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlEncodedParams = params)

        val request: RequestHandler<Array<Location>> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponseWithPaginationHeaders()
    }

    @GetMapping("/scpi/sender/2.2/locations/page/{uid}")
    fun getLocationPageFromDataOwner(@RequestHeader("authorization") authorization: String,
                                     @RequestHeader("SCN-Signature") signature: String? = null,
                                     @RequestHeader("X-Request-ID") requestID: String,
                                     @RequestHeader("X-Correlation-ID") correlationID: String,
                                     @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                     @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                     @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                     @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                     @PathVariable uid: String): ResponseEntity<ScpiResponse<Array<Location>>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = uid)

        val request: RequestHandler<Array<Location>> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest(proxied = true)
                .getResponseWithPaginationHeaders()
    }

    @GetMapping("/scpi/sender/2.2/locations/{locationID}")
    fun getLocationObjectFromDataOwner(@RequestHeader("authorization") authorization: String,
                                       @RequestHeader("X-Request-ID") requestID: String,
                                       @RequestHeader("SCN-Signature") signature: String? = null,
                                       @RequestHeader("X-Correlation-ID") correlationID: String,
                                       @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                       @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                       @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                       @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                       @PathVariable locationID: String): ResponseEntity<ScpiResponse<Location>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = locationID)

        val request: RequestHandler<Location> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @GetMapping("/scpi/sender/2.2/locations/{locationID}/{evseUID}")
    fun getEvseObjectFromDataOwner(@RequestHeader("authorization") authorization: String,
                                   @RequestHeader("SCN-Signature") signature: String? = null,
                                   @RequestHeader("X-Request-ID") requestID: String,
                                   @RequestHeader("X-Correlation-ID") correlationID: String,
                                   @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                   @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                   @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                   @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                   @PathVariable locationID: String,
                                   @PathVariable evseUID: String): ResponseEntity<ScpiResponse<Evse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$locationID/$evseUID")

        val request: RequestHandler<Evse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @GetMapping("/scpi/sender/2.2/locations/{locationID}/{evseUID}/{connectorID}")
    fun getConnectorObjectFromDataOwner(@RequestHeader("authorization") authorization: String,
                                        @RequestHeader("SCN-Signature") signature: String? = null,
                                        @RequestHeader("X-Request-ID") requestID: String,
                                        @RequestHeader("X-Correlation-ID") correlationID: String,
                                        @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                        @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                        @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                        @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                        @PathVariable locationID: String,
                                        @PathVariable evseUID: String,
                                        @PathVariable connectorID: String): ResponseEntity<ScpiResponse<Connector>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.SENDER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$locationID/$evseUID/$connectorID")

        val request: RequestHandler<Connector> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }


    /**
     * RECEIVER INTERFACES
     */

    @GetMapping("/scpi/receiver/2.2/locations/{countryCode}/{partyID}/{locationID}")
    fun getClientOwnedLocation(@RequestHeader("authorization") authorization: String,
                               @RequestHeader("SCN-Signature") signature: String? = null,
                               @RequestHeader("X-Request-ID") requestID: String,
                               @RequestHeader("X-Correlation-ID") correlationID: String,
                               @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                               @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                               @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                               @RequestHeader("SCPI-to-party-id") toPartyID: String,
                               @PathVariable countryCode: String,
                               @PathVariable partyID: String,
                               @PathVariable locationID: String): ResponseEntity<ScpiResponse<Location>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$locationID")

        val request: RequestHandler<Location> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @GetMapping("/scpi/receiver/2.2/locations/{countryCode}/{partyID}/{locationID}/{evseUID}")
    fun getClientOwnedEvse(@RequestHeader("authorization") authorization: String,
                           @RequestHeader("SCN-Signature") signature: String? = null,
                           @RequestHeader("X-Request-ID") requestID: String,
                           @RequestHeader("X-Correlation-ID") correlationID: String,
                           @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                           @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                           @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                           @RequestHeader("SCPI-to-party-id") toPartyID: String,
                           @PathVariable countryCode: String,
                           @PathVariable partyID: String,
                           @PathVariable locationID: String,
                           @PathVariable evseUID: String): ResponseEntity<ScpiResponse<Evse>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$locationID/$evseUID")

        val request: RequestHandler<Evse> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @GetMapping("/scpi/receiver/2.2/locations/{countryCode}/{partyID}/{locationID}/{evseUID}/{connectorID}")
    fun getClientOwnedConnector(@RequestHeader("authorization") authorization: String,
                                @RequestHeader("SCN-Signature") signature: String? = null,
                                @RequestHeader("X-Request-ID") requestID: String,
                                @RequestHeader("X-Correlation-ID") correlationID: String,
                                @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                @PathVariable countryCode: String,
                                @PathVariable partyID: String,
                                @PathVariable locationID: String,
                                @PathVariable evseUID: String,
                                @PathVariable connectorID: String): ResponseEntity<ScpiResponse<Connector>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.GET,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$locationID/$evseUID/$connectorID")

        val request: RequestHandler<Connector> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @PutMapping("/scpi/receiver/2.2/locations/{countryCode}/{partyID}/{locationID}")
    fun putClientOwnedLocation(@RequestHeader("authorization") authorization: String,
                               @RequestHeader("SCN-Signature") signature: String? = null,
                               @RequestHeader("X-Request-ID") requestID: String,
                               @RequestHeader("X-Correlation-ID") correlationID: String,
                               @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                               @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                               @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                               @RequestHeader("SCPI-to-party-id") toPartyID: String,
                               @PathVariable countryCode: String,
                               @PathVariable partyID: String,
                               @PathVariable locationID: String,
                               @RequestBody body: Location): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$locationID",
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @PutMapping("/scpi/receiver/2.2/locations/{countryCode}/{partyID}/{locationID}/{evseUID}")
    fun putClientOwnedEvse(@RequestHeader("authorization") authorization: String,
                           @RequestHeader("SCN-Signature") signature: String? = null,
                           @RequestHeader("X-Request-ID") requestID: String,
                           @RequestHeader("X-Correlation-ID") correlationID: String,
                           @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                           @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                           @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                           @RequestHeader("SCPI-to-party-id") toPartyID: String,
                           @PathVariable countryCode: String,
                           @PathVariable partyID: String,
                           @PathVariable locationID: String,
                           @PathVariable evseUID: String,
                           @RequestBody body: Evse): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$locationID/$evseUID",
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @PutMapping("/scpi/receiver/2.2/locations/{countryCode}/{partyID}/{locationID}/{evseUID}/{connectorID}")
    fun putClientOwnedConnector(@RequestHeader("authorization") authorization: String,
                                @RequestHeader("SCN-Signature") signature: String? = null,
                                @RequestHeader("X-Request-ID") requestID: String,
                                @RequestHeader("X-Correlation-ID") correlationID: String,
                                @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                @PathVariable countryCode: String,
                                @PathVariable partyID: String,
                                @PathVariable locationID: String,
                                @PathVariable evseUID: String,
                                @PathVariable connectorID: String,
                                @RequestBody body: Connector): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PUT,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$locationID/$evseUID/$connectorID",
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @PatchMapping("/scpi/receiver/2.2/locations/{countryCode}/{partyID}/{locationID}")
    fun patchClientOwnedLocation(@RequestHeader("authorization") authorization: String,
                                 @RequestHeader("SCN-Signature") signature: String? = null,
                                 @RequestHeader("X-Request-ID") requestID: String,
                                 @RequestHeader("X-Correlation-ID") correlationID: String,
                                 @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                 @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                 @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                 @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                 @PathVariable countryCode: String,
                                 @PathVariable partyID: String,
                                 @PathVariable locationID: String,
                                 @RequestBody body: Map<String, Any>): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PATCH,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$locationID",
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @PatchMapping("/scpi/receiver/2.2/locations/{countryCode}/{partyID}/{locationID}/{evseUID}")
    fun patchClientOwnedEvse(@RequestHeader("authorization") authorization: String,
                             @RequestHeader("SCN-Signature") signature: String? = null,
                             @RequestHeader("X-Request-ID") requestID: String,
                             @RequestHeader("X-Correlation-ID") correlationID: String,
                             @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                             @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                             @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                             @RequestHeader("SCPI-to-party-id") toPartyID: String,
                             @PathVariable countryCode: String,
                             @PathVariable partyID: String,
                             @PathVariable locationID: String,
                             @PathVariable evseUID: String,
                             @RequestBody body: Map<String, Any>): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PATCH,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$locationID/$evseUID",
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

    @PatchMapping("/scpi/receiver/2.2/locations/{countryCode}/{partyID}/{locationID}/{evseUID}/{connectorID}")
    fun patchClientOwnedConnector(@RequestHeader("authorization") authorization: String,
                                  @RequestHeader("SCN-Signature") signature: String? = null,
                                  @RequestHeader("X-Request-ID") requestID: String,
                                  @RequestHeader("X-Correlation-ID") correlationID: String,
                                  @RequestHeader("SCPI-from-country-code") fromCountryCode: String,
                                  @RequestHeader("SCPI-from-party-id") fromPartyID: String,
                                  @RequestHeader("SCPI-to-country-code") toCountryCode: String,
                                  @RequestHeader("SCPI-to-party-id") toPartyID: String,
                                  @PathVariable countryCode: String,
                                  @PathVariable partyID: String,
                                  @PathVariable locationID: String,
                                  @PathVariable evseUID: String,
                                  @PathVariable connectorID: String,
                                  @RequestBody body: Map<String, Any>): ResponseEntity<ScpiResponse<Unit>> {

        val sender = BasicRole(fromPartyID, fromCountryCode)
        val receiver = BasicRole(toPartyID, toCountryCode)

        val requestVariables = ScpiRequestVariables(
                module = ModuleID.LOCATIONS,
                interfaceRole = InterfaceRole.RECEIVER,
                method = HttpMethod.PATCH,
                headers = ScnHeaders(authorization, signature, requestID, correlationID, sender, receiver),
                urlPathVariables = "/$countryCode/$partyID/$locationID/$evseUID/$connectorID",
                body = body)

        val request: RequestHandler<Unit> = requestHandlerBuilder.build(requestVariables)
        return request
                .validateSender()
                .forwardRequest()
                .getResponse()
    }

}
