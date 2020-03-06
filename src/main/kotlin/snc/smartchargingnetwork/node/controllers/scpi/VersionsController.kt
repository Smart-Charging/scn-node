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

package snc.smartchargingnetwork.node.controllers.scpi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import snc.smartchargingnetwork.node.repositories.PlatformRepository
import snc.smartchargingnetwork.node.config.NodeProperties
import snc.smartchargingnetwork.node.models.scpi.InterfaceRole
import snc.smartchargingnetwork.node.models.scpi.ModuleID
import snc.smartchargingnetwork.node.models.scpi.ScpiStatus
import snc.smartchargingnetwork.node.models.exceptions.ScpiClientInvalidParametersException
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.tools.extractToken
import snc.smartchargingnetwork.node.tools.urlJoin

@RestController
@RequestMapping("/scpi")
class VersionsController(private val repository: PlatformRepository,
                         private val properties: NodeProperties) {

    @GetMapping("/versions")
    fun getVersions(@RequestHeader("Authorization") authorization: String): ScpiResponse<List<Version>> {

        val token = authorization.extractToken()
        val endpoint = urlJoin(properties.url, "/scpi/2.2")
        val versions = listOf(Version("2.2", endpoint))
        val response = ScpiResponse(ScpiStatus.SUCCESS.code, data = versions)

        return when {
            repository.existsByAuth_TokenA(token) -> response
            repository.existsByAuth_TokenC(token) -> response
            else -> throw ScpiClientInvalidParametersException("Invalid CREDENTIALS_TOKEN_A")
        }
    }

    @GetMapping("/2.2")
    fun getVersionsDetail(@RequestHeader("Authorization") authorization: String): ScpiResponse<VersionDetail> {

        val token = authorization.extractToken()
        val endpoints = this.getEndpoints()
        val response = ScpiResponse(
                    ScpiStatus.SUCCESS.code,
                    data = VersionDetail("2.2", endpoints))

        return when {
            repository.existsByAuth_TokenA(token) -> response
            repository.existsByAuth_TokenC(token) -> response
            else -> throw ScpiClientInvalidParametersException("Invalid CREDENTIALS_TOKEN_A")
        }
    }


    private fun getEndpoints(): List<Endpoint> {
        val endpoints = mutableListOf<Endpoint>()

        for (module in ModuleID.values()) {

            if (module.id == "hubclientinfo") {
                continue
            }

            if (module.id == "credentials" /*|| /module.id == "hubclientinfo"*/) {
                // these modules have only SENDER endpoint (the broker/hub)
                endpoints.add(Endpoint(
                        identifier = module.id,
                        role = InterfaceRole.SENDER,
                        url = urlJoin(properties.url, "/scpi/2.2/${module.id}")))
            } else {
                // remaining modules have both interfaces implemented
                endpoints.addAll(listOf(
                        Endpoint(
                                identifier = module.id,
                                role = InterfaceRole.SENDER,
                                url = urlJoin(properties.url, "/scpi/sender/2.2/${module.id}")),
                        Endpoint(
                                identifier = module.id,
                                role = InterfaceRole.RECEIVER,
                                url = urlJoin(properties.url, "/scpi/receiver/2.2/${module.id}"))))
            }
        }

        // custom module
        endpoints.add(Endpoint(
                identifier = "scnrules",
                role = InterfaceRole.RECEIVER,
                url = urlJoin(properties.url, "/scpi/2.2/receiver/scnrules")
        ))

        return endpoints
    }

}
