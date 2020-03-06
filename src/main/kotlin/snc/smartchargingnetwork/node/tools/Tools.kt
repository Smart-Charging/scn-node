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

package snc.smartchargingnetwork.node.tools

import org.web3j.crypto.Keys
import snc.smartchargingnetwork.node.models.HttpResponse
import snc.smartchargingnetwork.node.models.scpi.CommandResponse
import snc.smartchargingnetwork.node.models.scpi.CommandResponseType
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

fun generateUUIDv4Token(): String {
    return UUID.randomUUID().toString()
}

fun urlJoin(base: String, vararg paths: String?): String {
    var url = if (base.endsWith("/")) {
        base.dropLast(1)
    } else {
        base
    }
    for (path in paths) {
        if (path == null) {
            continue
        }
        val sanitizedPath: String = if (path.startsWith("/") && !path.endsWith("/")) {
            path
        } else if (path.startsWith("/") && path.endsWith("/")) {
            path.dropLast(1)
        } else if (!path.startsWith("/") && path.endsWith("/")) {
            "/${path.dropLast(1)}"
        } else {
            "/$path"
        }
        url += (sanitizedPath)
    }
    return url
}

fun getTimestamp(): String {
    return DateTimeFormatter.ISO_INSTANT.format(Instant.now())
}

fun generatePrivateKey(): String {
    val keys = Keys.createEcKeyPair()
    return keys.privateKey.toString(16)
}
