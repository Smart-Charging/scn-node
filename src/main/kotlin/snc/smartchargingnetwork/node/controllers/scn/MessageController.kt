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

package snc.smartchargingnetwork.node.controllers.scn

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import snc.smartchargingnetwork.node.services.RequestHandler
import snc.smartchargingnetwork.node.services.RequestHandlerBuilder
import snc.smartchargingnetwork.node.models.scpi.ScpiResponse


@RestController
@RequestMapping("/scn/message")
class MessageController(private val requestHandlerBuilder: RequestHandlerBuilder) {


    @PostMapping
    fun postMessage(@RequestHeader("X-Request-ID") requestID: String,
                    @RequestHeader("SCN-Signature") signature: String,
                    @RequestBody body: String): ResponseEntity<ScpiResponse<Any>> {

        val request: RequestHandler<Any> = requestHandlerBuilder.build(body)

        return request
                .validateScnMessage(signature)
                .forwardRequest()
                .getResponseWithAllHeaders()
    }

}
