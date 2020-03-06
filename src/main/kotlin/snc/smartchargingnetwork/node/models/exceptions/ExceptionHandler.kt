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

package snc.smartchargingnetwork.node.models.exceptions

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import snc.smartchargingnetwork.node.models.scpi.ScpiStatus
import snc.smartchargingnetwork.node.models.scpi.ScpiResponse
import java.net.ConnectException
import java.net.SocketTimeoutException

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class ExceptionHandler: ResponseEntityExceptionHandler() {

    override fun handleHttpMessageNotReadable(e: HttpMessageNotReadableException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ScpiResponse<Nothing>(
                statusCode = ScpiStatus.CLIENT_INVALID_PARAMETERS.code,
                statusMessage = e.message))
    }

    override fun handleMissingServletRequestParameter(e: MissingServletRequestParameterException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ScpiResponse<Nothing>(
                statusCode = ScpiStatus.CLIENT_INVALID_PARAMETERS.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingRequestHeaderException(e: MissingRequestHeaderException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ScpiResponse(
                statusCode = ScpiStatus.CLIENT_INVALID_PARAMETERS.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(SocketTimeoutException::class)
    fun handleSocketTimeoutException(e: SocketTimeoutException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.OK).body(ScpiResponse(
                statusCode = ScpiStatus.HUB_REQUEST_TIMEOUT.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ConnectException::class)
    fun handleConnectException(e: ConnectException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.OK).body(ScpiResponse(
                statusCode = ScpiStatus.HUB_CONNECTION_PROBLEM.code,
                statusMessage = e.message))
    }

    /**
     * SCPI EXCEPTIONS
     */

    @ExceptionHandler(ScpiClientGenericException::class)
    fun handleScpiClientGenericException(e: ScpiClientGenericException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiClientInvalidParametersException::class)
    fun handleScpiClientInvalidParametersException(e: ScpiClientInvalidParametersException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiClientNotEnoughInformationException::class)
    fun handleScpiClientNotEnoughInformationException(e: ScpiClientNotEnoughInformationException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiClientUnknownLocationException::class)
    fun handleScpiClientUnknownLocationException(e: ScpiClientUnknownLocationException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiServerGenericException::class)
    fun handleScpiServerGenericException(e: ScpiServerGenericException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiServerUnusableApiException::class)
    fun handleScpiServerUnusableApiException(e: ScpiServerUnusableApiException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiServerNoMatchingEndpointsException::class)
    fun handleScpiServerNoMatchingEndpointsException(e: ScpiServerNoMatchingEndpointsException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiServerUnsupportedVersionException::class)
    fun handleScpiServerUnsupportedVersionException(e: ScpiServerUnsupportedVersionException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiHubConnectionProblemException::class)
    fun handleScpiHubConnectionProblemException(e: ScpiHubConnectionProblemException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiHubTimeoutOnRequestException::class)
    fun handleScpiHubTimeoutOnRequestException(e: ScpiHubTimeoutOnRequestException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

    @ExceptionHandler(ScpiHubUnknownReceiverException::class)
    fun handleScpiHubUnknownReceiverException(e: ScpiHubUnknownReceiverException): ResponseEntity<ScpiResponse<Nothing>> {
        return ResponseEntity.status(e.httpStatus).body(ScpiResponse(
                statusCode = e.scpiStatus.code,
                statusMessage = e.message))
    }

}
