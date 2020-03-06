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

package snc.smartchargingnetwork.node.models

import smartcharging.openchargingnetwork.notary.SignableHeaders
import smartcharging.openchargingnetwork.notary.ValuesToSign
import snc.smartchargingnetwork.node.models.scpi.ScpiResponse

data class HttpResponse<T: Any>(val statusCode: Int,
                                val headers: Map<String, String>,
                                val body: ScpiResponse<T>) {
    fun toSignedValues(): ValuesToSign<ScpiResponse<T>> {
        return ValuesToSign(
                headers = SignableHeaders(
                        limit = headers["X-Limit"] ?: headers["x-limit"],
                        totalCount = headers["X-Total-Count"] ?: headers["x-total-count"],
                        link = headers["Link"] ?: headers["link"],
                        location = headers["Location"] ?: headers["location"]
                ),
                body = body)
    }
}
