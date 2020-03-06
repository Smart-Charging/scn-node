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

import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import snc.smartchargingnetwork.node.models.ScnRules
import snc.smartchargingnetwork.node.models.ScnRulesListParty
import snc.smartchargingnetwork.node.models.scpi.BasicRole
import snc.smartchargingnetwork.node.models.scpi.ScpiResponse
import snc.smartchargingnetwork.node.services.ScnRulesService


@RestController
class ScnRulesController(private val scnRulesService: ScnRulesService) {

    @GetMapping("/scpi/receiver/2.2/scnrules")
    fun getRules(@RequestHeader("authorization") authorization: String): ResponseEntity<ScpiResponse<ScnRules>> {
        return ResponseEntity.ok(ScpiResponse(
                statusCode = 1000,
                data = scnRulesService.getRules(authorization)))
    }

    @Transactional
    @PostMapping("/scpi/receiver/2.2/scnrules/whitelist")
    fun appendToWhitelist(@RequestHeader("authorization") authorization: String,
                          @RequestBody body: ScnRulesListParty): ResponseEntity<ScpiResponse<Unit>> {

        scnRulesService.appendToWhitelist(authorization, body)
        return ResponseEntity.ok(ScpiResponse(statusCode = 1000))
    }

    @Transactional
    @PostMapping("/scpi/receiver/2.2/scnrules/blacklist")
    fun appendToBlacklist(@RequestHeader("authorization") authorization: String,
                          @RequestBody body: ScnRulesListParty): ResponseEntity<ScpiResponse<Unit>> {

        scnRulesService.appendToBlacklist(authorization, body)
        return ResponseEntity.ok(ScpiResponse(statusCode = 1000))
    }

    @Transactional
    @PutMapping("/scpi/receiver/2.2/scnrules/signatures")
    fun updateSignatures(@RequestHeader("authorization") authorization: String): ResponseEntity<ScpiResponse<Unit>> {

        scnRulesService.updateSignatures(authorization)
        return ResponseEntity.ok(ScpiResponse(statusCode = 1000))
    }

    @Transactional
    @PutMapping("/scpi/receiver/2.2/scnrules/whitelist")
    fun updateWhitelist(@RequestHeader("authorization") authorization: String,
                        @RequestBody body: List<ScnRulesListParty>): ResponseEntity<ScpiResponse<Unit>> {

        scnRulesService.updateWhitelist(authorization, body)
        return ResponseEntity.ok(ScpiResponse(statusCode = 1000))
    }

    @Transactional
    @PutMapping("/scpi/receiver/2.2/scnrules/blacklist")
    fun updateBlacklist(@RequestHeader("authorization") authorization: String,
                        @RequestBody body: List<ScnRulesListParty>): ResponseEntity<ScpiResponse<Unit>> {

        scnRulesService.updateBlacklist(authorization, body)
        return ResponseEntity.ok(ScpiResponse(statusCode = 1000))
    }

    @Transactional
    @PutMapping("/scpi/receiver/2.2/scnrules/block-all")
    fun blockAll(@RequestHeader("authorization") authorization: String): ResponseEntity<ScpiResponse<Unit>> {

        scnRulesService.blockAll(authorization)
        return ResponseEntity.ok(ScpiResponse(statusCode = 1000))
    }

    @Transactional
    @DeleteMapping("scpi/receiver/2.2/scnrules/whitelist/{countryCode}/{partyID}")
    fun deleteFromWhitelist(@RequestHeader("authorization") authorization: String,
                            @PathVariable countryCode: String,
                            @PathVariable partyID: String): ResponseEntity<ScpiResponse<Unit>> {

        val party = BasicRole(country = countryCode, id = partyID).toUpperCase()
        scnRulesService.deleteFromWhitelist(authorization, party)
        return ResponseEntity.ok(ScpiResponse(statusCode = 1000))
    }

    @Transactional
    @DeleteMapping("scpi/receiver/2.2/scnrules/blacklist/{countryCode}/{partyID}")
    fun deleteFromBlacklist(@RequestHeader("authorization") authorization: String,
                            @PathVariable countryCode: String,
                            @PathVariable partyID: String): ResponseEntity<ScpiResponse<Unit>> {

        val party = BasicRole(country = countryCode, id = partyID).toUpperCase()
        scnRulesService.deleteFromBlacklist(authorization, party)
        return ResponseEntity.ok(ScpiResponse(statusCode = 1000))
    }
}
