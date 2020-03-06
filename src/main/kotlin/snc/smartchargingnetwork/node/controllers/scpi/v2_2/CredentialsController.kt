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

import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import snc.smartchargingnetwork.node.repositories.*
import snc.smartchargingnetwork.node.config.NodeProperties
import snc.smartchargingnetwork.node.models.scpi.ConnectionStatus
import snc.smartchargingnetwork.node.models.scpi.Role
import snc.smartchargingnetwork.node.models.scpi.ScpiStatus
import snc.smartchargingnetwork.node.models.entities.Auth
import snc.smartchargingnetwork.node.models.entities.EndpointEntity
import snc.smartchargingnetwork.node.models.entities.RoleEntity
import snc.smartchargingnetwork.node.models.exceptions.ScpiClientInvalidParametersException
import snc.smartchargingnetwork.node.models.exceptions.ScpiServerNoMatchingEndpointsException
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.services.HttpService
import snc.smartchargingnetwork.node.services.RoutingService
import snc.smartchargingnetwork.node.tools.*


@RestController
@RequestMapping("/scpi/2.2/credentials")
class CredentialsController(private val platformRepo: PlatformRepository,
                            private val roleRepo: RoleRepository,
                            private val endpointRepo: EndpointRepository,
                            private val scnRulesListRepo: ScnRulesListRepository,
                            private val properties: NodeProperties,
                            private val routingService: RoutingService,
                            private val httpService: HttpService) {

    private fun myCredentials(token: String): Credentials {
        return Credentials(
                token = token,
                url = urlJoin(properties.url, "/scpi/versions"),
                roles = listOf(CredentialsRole(
                        role = Role.HUB,
                        businessDetails = BusinessDetails(name = "Smart Charging Network Node"),
                        partyID = "SCN",
                        countryCode = "CH")))
    }

    @GetMapping
    fun getCredentials(@RequestHeader("Authorization") authorization: String): ScpiResponse<Credentials> {

        // TODO: allow token A authorization

        return platformRepo.findByAuth_TokenC(authorization.extractToken())?.let {

            ScpiResponse(
                    statusCode = ScpiStatus.SUCCESS.code,
                    data = myCredentials(it.auth.tokenC!!))

        } ?: throw ScpiClientInvalidParametersException("Invalid CREDENTIALS_TOKEN_C")
    }

    @PostMapping
    @Transactional
    fun postCredentials(@RequestHeader("Authorization") authorization: String,
                        @RequestBody body: Credentials): ScpiResponse<Credentials> {

        // check platform previously registered by admin
        val platform = platformRepo.findByAuth_TokenA(authorization.extractToken())
                ?: throw ScpiClientInvalidParametersException("Invalid CREDENTIALS_TOKEN_A")

        // GET versions information endpoint with TOKEN_B (both provided in request body)
        val versionsInfo = httpService.getVersions(body.url, body.token)

        // try to match version 2.2
        val correctVersion = versionsInfo.firstOrNull { it.version == "2.2" }
                ?: throw ScpiServerNoMatchingEndpointsException("Expected version 2.2 from $versionsInfo")

        // GET 2.2 version details
        val versionDetail = httpService.getVersionDetail(correctVersion.url, body.token)

        // ensure each role does not already exist
        for (role in body.roles) {
            val basicRole = BasicRole(role.partyID, role.countryCode)
            if (!routingService.isRoleKnownOnNetwork(basicRole)) {
                throw ScpiClientInvalidParametersException("Role with party_id=${basicRole.id} and country_code=${basicRole.country} not listed in SCN Registry with my node info!")
            }
            if (roleRepo.existsByCountryCodeAndPartyIDAllIgnoreCase(basicRole.country, basicRole.id)) {
                throw ScpiClientInvalidParametersException("Role with party_id=${basicRole.id} and country_code=${basicRole.country} already connected to this node!")
            }
        }

        // generate TOKEN_C
        val tokenC = generateUUIDv4Token()

        // set platform connection details
        platform.auth = Auth(tokenA = null, tokenB = body.token, tokenC = tokenC)
        platform.versionsUrl = body.url
        platform.status = ConnectionStatus.CONNECTED
        platform.lastUpdated = getTimestamp()
        platform.rules.signatures = properties.signatures

        // set platform's roles' credentials
        val roles = mutableListOf<RoleEntity>()

        for (role in body.roles) {
            roles.add(RoleEntity(
                    platformID = platform.id!!,
                    role = role.role,
                    businessDetails = role.businessDetails,
                    partyID = role.partyID,
                    countryCode = role.countryCode))
        }

        platformRepo.save(platform)
        roleRepo.saveAll(roles)

        // set platform's endpoints
        for (endpoint in versionDetail.endpoints) {
            endpointRepo.save(EndpointEntity(
                    platformID = platform.id!!,
                    identifier = endpoint.identifier,
                    role = endpoint.role,
                    url = endpoint.url
            ))
        }

        // return SCN's platform connection information and role credentials
        return ScpiResponse(
                statusCode = ScpiStatus.SUCCESS.code,
                data = myCredentials(tokenC))
    }

    @PutMapping
    @Transactional
    fun putCredentials(@RequestHeader("Authorization") authorization: String,
                       @RequestBody body: Credentials): ScpiResponse<Credentials> {

        // find platform (required to have already been fully registered)
        val platform = platformRepo.findByAuth_TokenC(authorization.extractToken())
                ?: throw ScpiClientInvalidParametersException("Invalid CREDENTIALS_TOKEN_C")

        // GET versions information endpoint with TOKEN_B (both provided in request body)
        val versionsInfo: List<Version> = httpService.getVersions(body.url, body.token)

        // try to match version 2.2
        val correctVersion = versionsInfo.firstOrNull { it.version == "2.2" }
                ?: throw ScpiClientInvalidParametersException("Expected version 2.2 from ${body.url}")

        // GET 2.2 version details
        val versionDetail = httpService.getVersionDetail(correctVersion.url, body.token)

        // generate TOKEN_C
        val tokenC = generateUUIDv4Token()

        // set platform connection information
        platform.auth = Auth(tokenA = null, tokenB = body.token, tokenC = tokenC)
        platform.versionsUrl = body.url
        platform.status = ConnectionStatus.CONNECTED
        platform.lastUpdated = getTimestamp()

        endpointRepo.deleteByPlatformID(platform.id)
        roleRepo.deleteByPlatformID(platform.id)

        // set platform's roles' credentials
        val roles = mutableListOf<RoleEntity>()

        for (role in body.roles) {
            roles.add(RoleEntity(
                    platformID = platform.id!!,
                    role = role.role,
                    businessDetails = role.businessDetails,
                    partyID = role.partyID,
                    countryCode = role.countryCode))
        }

        platformRepo.save(platform)
        roleRepo.saveAll(roles)

        // set platform's endpoints
        for (endpoint in versionDetail.endpoints) {
            endpointRepo.save(EndpointEntity(
                    platformID = platform.id!!,
                    identifier = endpoint.identifier,
                    role = endpoint.role,
                    url = endpoint.url))
        }

        // return SCN Node's platform connection information and role credentials (same for all nodes)
        return ScpiResponse(
                statusCode = ScpiStatus.SUCCESS.code,
                data = myCredentials(tokenC))
    }

    @DeleteMapping
    @Transactional
    fun deleteCredentials(@RequestHeader("Authorization") authorization: String): ScpiResponse<Nothing?> {

        val platform = platformRepo.findByAuth_TokenC(authorization.extractToken())
                ?: throw ScpiClientInvalidParametersException("Invalid CREDENTIALS_TOKEN_C")

        platformRepo.deleteById(platform.id!!)
        roleRepo.deleteByPlatformID(platform.id)
        endpointRepo.deleteByPlatformID(platform.id)
        scnRulesListRepo.deleteByPlatformID(platform.id)

        return ScpiResponse(statusCode = 1000, data = null)
    }

}
