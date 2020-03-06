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

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import snc.smartchargingnetwork.node.models.*
import snc.smartchargingnetwork.node.models.exceptions.ScpiServerGenericException
import snc.smartchargingnetwork.node.models.exceptions.ScpiServerUnusableApiException
import snc.smartchargingnetwork.node.models.scpi.*
import snc.smartchargingnetwork.node.tools.urlJoin


@Service
class HttpService {

    val mapper = jacksonObjectMapper()

    fun convertToRequestVariables(stringBody: String): ScpiRequestVariables = mapper.readValue(stringBody)


    /**
     * Generic HTTP request expecting a response of type ScpiResponse<T> as defined by the caller
     */
    fun <T : Any> makeScpiRequest(method: HttpMethod, url: String, headers: Map<String, String?>, params: Map<String, Any?>? = null, json: Map<String, Any>? = null): HttpResponse<T> {
        val paramsWithStringValues = params?.mapValues { (_, value) -> value.toString() } ?: mapOf()
        val response = when (method) {
            HttpMethod.GET -> khttp.get(url, headers, paramsWithStringValues)
            HttpMethod.POST -> khttp.post(url, headers, paramsWithStringValues, json = json)
            HttpMethod.PUT -> khttp.put(url, headers, paramsWithStringValues, json = json)
            HttpMethod.PATCH -> khttp.patch(url, headers, paramsWithStringValues, json = json)
            HttpMethod.DELETE -> khttp.delete(url, headers)
            else -> throw IllegalStateException("Invalid method: $method")
        }

        try {
            return HttpResponse(
                    statusCode = response.statusCode,
                    headers = response.headers,
                    body = mapper.readValue(response.text))
        } catch (e: JsonParseException) {
            throw ScpiServerGenericException("Could not parse JSON response of forwarded SCPI request: ${e.message}")
        }
    }


    /**
     * Generic HTTP request expecting a response of type ScpiResponse<T> as defined by the caller
     */
    final fun <T: Any> makeScpiRequest(url: String,
                                       headers: ScnHeaders,
                                       requestVariables: ScpiRequestVariables): HttpResponse<T> {

        val headersMap = headers.toMap()

        var jsonBody: Map<String,Any>? = null
        if (requestVariables.body != null) {
            val jsonString = mapper.writeValueAsString(requestVariables.body)
            jsonBody = mapper.readValue(jsonString)
        }

        return makeScpiRequest(
                method = requestVariables.method,
                url = url,
                headers = headersMap,
                params = requestVariables.urlEncodedParams,
                json = jsonBody)
    }


    /**
     * Get SCPI versions during the Credentials registration handshake
     */
    fun getVersions(url: String, authorization: String): List<Version> {
        try {
            val response = khttp.get(url = url, headers = mapOf("Authorization" to "Token $authorization"))
            val body: ScpiResponse<List<Version>> = mapper.readValue(response.text)

            return if (response.statusCode == 200 && body.statusCode == 1000) {
                body.data!!
            } else {
                throw Exception("Returned HTTP status code ${response.statusCode}; SCPI status code ${body.statusCode} - ${body.statusMessage}")
            }

        } catch (e: Exception) {
            throw ScpiServerUnusableApiException("Failed to request from $url: ${e.message}")
        }
    }


    /**
     * Get version details (using result of above getVersions request) during the Credentials registration handshake
     * Will provide SCN Node with modules implemented by SCPI platform and their endpoints
     */
    fun getVersionDetail(url: String, authorization: String): VersionDetail {
        try {
            val response = khttp.get(url = url, headers = mapOf("Authorization" to "Token $authorization"))
            val body: ScpiResponse<VersionDetail> = mapper.readValue(response.text)

            return if (response.statusCode == 200 && body.statusCode == 1000) {
                body.data!!
            } else {
                throw Exception("Returned HTTP status code ${response.statusCode}; SCPI status code ${body.statusCode} - ${body.statusMessage}")
            }

        } catch (e: Exception) {
            throw ScpiServerUnusableApiException("Failed to request v2.2 details from $url: ${e.message}")
        }
    }


    /**
     * Make a POST request to an SCN Node which implements /scn/message
     * Used to forward requests to SCPI platforms of which the SCN Node does not share a local connection with
     */
    final fun <T: Any> postScnMessage(url: String,
                                headers: ScnMessageHeaders,
                                body: String): HttpResponse<T> {

        val headersMap = headers.toMap()

        val fullURL = urlJoin(url, "/scn/message")

        val response = khttp.post(fullURL, headersMap, data = body)

        return HttpResponse(
                statusCode = response.statusCode,
                headers = response.headers,
                body = mapper.readValue(response.text))
    }

}
