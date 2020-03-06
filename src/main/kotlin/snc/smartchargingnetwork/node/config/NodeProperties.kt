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

package snc.smartchargingnetwork.node.config

import org.springframework.boot.context.properties.ConfigurationProperties
import snc.smartchargingnetwork.node.tools.generateUUIDv4Token

@ConfigurationProperties("scn.node")
class NodeProperties {

    var apikey: String = generateUUIDv4Token()

    var dev: Boolean = false

    var privateKey: String? = null

    var signatures: Boolean = true

    lateinit var url: String

    var web3 = Web3()

    class Web3 {

        lateinit var provider: String

        var contracts = Contracts()

        class Contracts {
            lateinit var registry: String
        }
    }
}
