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

package snc.smartchargingnetwork.node.services

import org.springframework.stereotype.Service
import snc.smartchargingnetwork.node.models.ScnRules
import snc.smartchargingnetwork.node.models.ScnRulesList
import snc.smartchargingnetwork.node.models.ScnRulesListParty
import snc.smartchargingnetwork.node.models.ScnRulesListType
import snc.smartchargingnetwork.node.models.entities.ScnRulesListEntity
import snc.smartchargingnetwork.node.models.entities.PlatformEntity
import snc.smartchargingnetwork.node.models.exceptions.ScpiClientGenericException
import snc.smartchargingnetwork.node.models.exceptions.ScpiClientInvalidParametersException
import snc.smartchargingnetwork.node.models.scpi.BasicRole
import snc.smartchargingnetwork.node.repositories.ScnRulesListRepository
import snc.smartchargingnetwork.node.repositories.PlatformRepository
import snc.smartchargingnetwork.node.tools.extractToken


@Service
class ScnRulesService(private val platformRepo: PlatformRepository,
                      private val scnRulesListRepo: ScnRulesListRepository) {

    fun getRules(authorization: String): ScnRules {
        val platform = findPlatform(authorization)

        val rulesList = scnRulesListRepo.findAllByPlatformID(platform.id).map { getModules(it) }

        return ScnRules(
                signatures = platform.rules.signatures,
                whitelist = ScnRulesList(
                        active = platform.rules.whitelist,
                        list = when (platform.rules.whitelist) {
                            true -> rulesList
                            false -> listOf()
                        }),
                blacklist = ScnRulesList(
                        active = platform.rules.blacklist,
                        list = when (platform.rules.blacklist) {
                            true -> rulesList
                            false -> listOf()
                        }))
    }

    private fun getModules(scnRulesListEntity: ScnRulesListEntity): ScnRulesListParty {
        val basicRole = scnRulesListEntity.counterparty
        val modules = mutableListOf<String>()

        if(scnRulesListEntity.cdrs) {
            modules.add("cdrs")
        }

        if(scnRulesListEntity.chargingprofiles) {
            modules.add("chargingprofiles")
        }

        if(scnRulesListEntity.commands) {
            modules.add("commands")
        }

        if(scnRulesListEntity.locations) {
            modules.add("locations")
        }

        if(scnRulesListEntity.sessions) {
            modules.add("sessions")
        }

        if(scnRulesListEntity.tariffs) {
            modules.add("tariffs")
        }

        if(scnRulesListEntity.tokens) {
            modules.add("tokens")
        }

        return ScnRulesListParty(
            id = basicRole.id,
            country =  basicRole.country,
            modules = modules
        )
    }

    fun updateSignatures(authorization: String) {
        val platform = findPlatform(authorization)
        platform.rules.signatures = !platform.rules.signatures
        platformRepo.save(platform)
    }

    fun blockAll(authorization: String) {
        // 1. check C / find platform
        val platform = findPlatform(authorization);

        // 2. determine whether whitelist is active
        assertListNotActive(platform, ScnRulesListType.BLACKLIST)

        // 3. set the whitelist to true with empty list
        platform.rules.whitelist = true
        scnRulesListRepo.deleteAll()

        // 4. save whitelist option
        platformRepo.save(platform)
    }

    fun updateWhitelist(authorization: String, parties: List<ScnRulesListParty>) {
        // 1. check if any module of party is not empty
        checkModuleList(parties)

        // 2. check token C / find platform
        val platform = findPlatform(authorization)

        // 3. determine whether whitelist is active
        platform.rules.whitelist = when (parties.count()) {
            // set to false if provided list is empty (deletes list)
            0 -> false
            else -> {
                // 2.1. check blacklist active
                assertListNotActive(platform, ScnRulesListType.BLACKLIST)
                // set to true if list not empty
                true
            }
        }

        // 4. save whitelist option
        platformRepo.save(platform)

        // 5. re-apply whitelist
        scnRulesListRepo.deleteByPlatformID(platform.id)

        scnRulesListRepo.saveAll(parties.map { ScnRulesListEntity(
            platformID = platform.id!!,
            counterparty = BasicRole(it.id, it.country).toUpperCase(),
            cdrs = it.modules.contains("cdrs"),
            chargingprofiles = it.modules.contains("chargingprofiles"),
            commands = it.modules.contains("commands"),
            sessions = it.modules.contains("sessions"),
            locations= it.modules.contains("locations"),
            tariffs = it.modules.contains("tariffs"),
            tokens = it.modules.contains("tokens")
        )})
    }

    fun updateBlacklist(authorization: String, parties: List<ScnRulesListParty>) {
        // 1. check if any module of party is not empty
        checkModuleList(parties)

        // 2. check token C / find platform
        val platform = findPlatform(authorization)

        // 3. determine whether blacklist is active
        platform.rules.blacklist = when (parties.count()) {
            // set to false if provided list is empty (delete list)
            0 -> false
            else -> {
                // 2.1 check whitelist is active
                assertListNotActive(platform, ScnRulesListType.WHITELIST)
                // set true if list is not empty
                true
            }
        }

        // 4. save blacklist option
        platformRepo.save(platform)

        // 5. re-apply blacklist
        scnRulesListRepo.deleteByPlatformID(platform.id)

        scnRulesListRepo.saveAll(parties.map { ScnRulesListEntity(
                platformID = platform.id!!,
                counterparty = BasicRole(it.id, it.country).toUpperCase(),
                cdrs = it.modules.contains("cdrs"),
                chargingprofiles = it.modules.contains("chargingprofiles"),
                commands = it.modules.contains("commands"),
                sessions = it.modules.contains("sessions"),
                locations= it.modules.contains("locations"),
                tariffs = it.modules.contains("tariffs"),
                tokens = it.modules.contains("tokens")
        )})
    }

    fun appendToWhitelist(authorization: String, body: ScnRulesListParty) {
        // 1. check module of party is not empty
        checkModule(body.modules)

        // 2. check token C / find platform
        val platform = findPlatform(authorization)

        // 3. check blacklist active
        assertListNotActive(platform, ScnRulesListType.BLACKLIST)

        // 4. set whitelist to true
        platform.rules.whitelist = true

        // 5. check entry does not already exist
        if (scnRulesListRepo.existsByCounterparty(BasicRole( id = body.id, country = body.country).toUpperCase())) {
            throw ScpiClientInvalidParametersException("Party already on SCN Rules whitelist")
        }

        // 6. save whitelist option
        platformRepo.save(platform)

        // 7. add to whitelist
        scnRulesListRepo.save(ScnRulesListEntity(
                platformID = platform.id!!,
                counterparty = BasicRole( id = body.id, country = body.country).toUpperCase(),
                cdrs = body.modules.contains("cdrs"),
                chargingprofiles = body.modules.contains("chargingprofiles"),
                commands = body.modules.contains("commands"),
                sessions = body.modules.contains("sessions"),
                locations = body.modules.contains("locations"),
                tariffs = body.modules.contains("tariffs"),
                tokens = body.modules.contains("tokens")
        ))
    }

    fun appendToBlacklist (authorization: String, body: ScnRulesListParty) {
        // 1. check module of party is not empty
        checkModule(body.modules)

        // 2. check token C/ find platform
        val platform = findPlatform(authorization)

        // 3. check whitelist active
        assertListNotActive(platform, ScnRulesListType.WHITELIST)

        // 4. set blacklist to true
        platform.rules.blacklist = true

        // 5. check entry does not already exist
        if (scnRulesListRepo.existsByCounterparty(BasicRole( id = body.id, country = body.country).toUpperCase())) {
            throw ScpiClientInvalidParametersException("Party already on SCN Rules blacklist")
        }

        // 6. save blacklist option
        platformRepo.save(platform)

        // 7. add to blacklist
        scnRulesListRepo.save(ScnRulesListEntity(
                platformID = platform.id!!,
                counterparty = BasicRole( id = body.id, country = body.country).toUpperCase(),
                cdrs = body.modules.contains("cdrs"),
                chargingprofiles = body.modules.contains("chargingprofiles"),
                commands = body.modules.contains("commands"),
                sessions = body.modules.contains("sessions"),
                locations = body.modules.contains("locations"),
                tariffs = body.modules.contains("tariffs"),
                tokens = body.modules.contains("tokens")
        ))
    }

    fun deleteFromWhitelist(authorization: String, party: BasicRole) {
        // 1. check token C / find platform
        val platform = findPlatform(authorization)

        // 2. check whitelist/blacklist activeness
        if (platform.rules.blacklist || !platform.rules.whitelist) {
            throw ScpiClientGenericException("Cannot delete entry from SCN Rules whitelist")
        }

        // 3. delete entry
        scnRulesListRepo.deleteByPlatformIDAndCounterparty(platform.id, party)

        // 4. set activeness
        platform.rules.whitelist = scnRulesListRepo.findAllByPlatformID(platform.id).count() >= 1
        platformRepo.save(platform)
    }

    fun deleteFromBlacklist(authorization: String, party: BasicRole) {
        // 1. check token C / find platform
        val platform = findPlatform(authorization)

        // 2. check blacklist/whitelist activeness
        if (platform.rules.whitelist || !platform.rules.blacklist) {
            throw ScpiClientGenericException("Cannot delete entry from SCN Rules blacklist")
        }

        // 3. delete entry
        scnRulesListRepo.deleteByPlatformIDAndCounterparty(platform.id, party)

        // 4. set activeness
        platform.rules.blacklist = scnRulesListRepo.findAllByPlatformID(platform.id).count() >= 1
        platformRepo.save(platform)
    }

    private fun findPlatform(authorization: String): PlatformEntity {
        return platformRepo.findByAuth_TokenC(authorization.extractToken())
                ?: throw ScpiClientInvalidParametersException("Invalid CREDENTIALS_TOKEN_C")
    }

    private fun checkModule(modules: List<String>) {
        val result = modules.any{ it.isNullOrEmpty() }

        if(result) {
            throw ScpiClientGenericException("Module list is empty")
        }
    }

    private fun checkModuleList(parties: List<ScnRulesListParty>) {
        // 1. check Module is empty or not
        var result = parties.any { it.modules.isNullOrEmpty() }

        if(result) {
            throw ScpiClientGenericException("Module list of one the party is empty")
        }

        // 2. check each element of module is empty or not
        result = parties.any { it -> it.modules.any { it.isNullOrEmpty() } }

        if(result) {
            throw ScpiClientGenericException("One of the element of module list is empty")
        }
    }

    private fun assertListNotActive(platform: PlatformEntity, type: ScnRulesListType) {
        val list = when (type) {
            ScnRulesListType.WHITELIST -> platform.rules.whitelist
            ScnRulesListType.BLACKLIST -> platform.rules.blacklist
        }
        if (list) {
            throw ScpiClientGenericException("SCN Rules whitelist and blacklist cannot be active at same time")
        }
    }

}
