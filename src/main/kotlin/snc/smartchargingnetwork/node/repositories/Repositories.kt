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

package snc.smartchargingnetwork.node.repositories

import org.springframework.data.repository.CrudRepository
import snc.smartchargingnetwork.node.models.entities.*
import snc.smartchargingnetwork.node.models.scpi.BasicRole
import snc.smartchargingnetwork.node.models.scpi.InterfaceRole

interface PlatformRepository: CrudRepository<PlatformEntity, Long> {
    fun existsByAuth_TokenA(tokenA: String?): Boolean
    fun existsByAuth_TokenC(tokenC: String?): Boolean
    fun findByAuth_TokenA(tokenA: String?): PlatformEntity?
    fun findByAuth_TokenC(tokenC: String?): PlatformEntity?
}

interface RoleRepository: CrudRepository<RoleEntity, Long> {
    // used in registration to prevent multiple roles of the same country_code/party_id combination
    fun existsByCountryCodeAndPartyIDAllIgnoreCase(countryCode: String, partyID: String): Boolean
    // used to ensure sender's role is registered to a platform on the broker (hub)
    fun existsByPlatformIDAndCountryCodeAndPartyIDAllIgnoreCase(platformID: Long?, countryCode: String, partyID: String): Boolean
    // used in routing to find roles registered with broker (hub)
    fun findByCountryCodeAndPartyIDAllIgnoreCase(countryCode: String, partyID: String): RoleEntity?
    fun findAllByCountryCodeAndPartyIDAllIgnoreCase(countryCode: String, partyID: String): Iterable<RoleEntity>
    fun findAllByPlatformID(platformID: Long?): Iterable<RoleEntity>
    fun deleteByPlatformID(platformID: Long?)
}

interface EndpointRepository: CrudRepository<EndpointEntity, Long> {
    fun findByPlatformID(platformID: Long?): Iterable<EndpointEntity>
    fun findByPlatformIDAndIdentifierAndRole(platformID: Long?, identifier: String, Role: InterfaceRole): EndpointEntity?
    fun deleteByPlatformID(platformID: Long?)
}

interface ProxyResourceRepository: CrudRepository<ProxyResourceEntity, Long> {
    fun findByIdAndSenderAndReceiver(id: Long?, sender: BasicRole, receiver: BasicRole): ProxyResourceEntity?
    fun findByAlternativeUIDAndSenderAndReceiver(alternativeUID: String, sender: BasicRole, receiver: BasicRole): ProxyResourceEntity?
}

interface ScnRulesListRepository: CrudRepository<ScnRulesListEntity, Long> {
    fun existsByCounterparty(party: BasicRole): Boolean
    fun findAllByPlatformID(platformID: Long?): Iterable<ScnRulesListEntity>
    fun deleteByPlatformID(platformID: Long?)
    fun deleteByPlatformIDAndCounterparty(platformID: Long?, party: BasicRole)
}
