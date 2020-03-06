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

import org.springframework.http.HttpStatus
import snc.smartchargingnetwork.node.models.scpi.ScpiStatus

// 2xxx: Client errors
class ScpiClientGenericException(message: String,
                                 val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
                                 val scpiStatus: ScpiStatus = ScpiStatus.CLIENT_ERROR): Exception(message)

class ScpiClientInvalidParametersException(message: String = "Invalid or missing parameters",
                                           val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
                                           val scpiStatus: ScpiStatus = ScpiStatus.CLIENT_INVALID_PARAMETERS): Exception(message)

class ScpiClientNotEnoughInformationException(message: String = "Not enough information",
                                              val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
                                              val scpiStatus: ScpiStatus = ScpiStatus.CLIENT_NOT_ENOUGH_INFO): Exception(message)

class ScpiClientUnknownLocationException(message: String = "Unknown location",
                                         val httpStatus: HttpStatus = HttpStatus.NOT_FOUND,
                                         val scpiStatus: ScpiStatus = ScpiStatus.CLIENT_UNKNOWN_LOCATION): Exception(message)

// 3xxx: Server errors
class ScpiServerGenericException(message: String,
                                 val httpStatus: HttpStatus = HttpStatus.OK,
                                 val scpiStatus: ScpiStatus = ScpiStatus.SERVER_ERROR): Exception(message)

class ScpiServerUnusableApiException(message: String = "Unable to use client's API",
                                     val httpStatus: HttpStatus = HttpStatus.OK,
                                     val scpiStatus: ScpiStatus = ScpiStatus.SERVER_UNUSABLE_API): Exception(message)

class ScpiServerUnsupportedVersionException(message: String = "Unsupported version",
                                            val httpStatus: HttpStatus = HttpStatus.OK,
                                            val scpiStatus: ScpiStatus = ScpiStatus.SERVER_UNSUPPORTED_VERSION): Exception(message)

class ScpiServerNoMatchingEndpointsException(message: String = "No matching endpoints or expected endpoints missing between parties",
                                             val httpStatus: HttpStatus = HttpStatus.OK,
                                             val scpiStatus: ScpiStatus = ScpiStatus.SERVER_NO_MATCHING_ENDPOINTS): Exception(message)

// 4xxx: Hub errors
class ScpiHubUnknownReceiverException(message: String = "Unknown receiver",
                                      val httpStatus: HttpStatus = HttpStatus.OK,
                                      val scpiStatus: ScpiStatus = ScpiStatus.HUB_UNKNOWN_RECEIVER): Exception(message)

class ScpiHubTimeoutOnRequestException(message: String = "Timeout on forwarded request",
                                       val httpStatus: HttpStatus = HttpStatus.OK,
                                       val scpiStatus: ScpiStatus = ScpiStatus.HUB_REQUEST_TIMEOUT): Exception(message)

class ScpiHubConnectionProblemException(message: String = "Connection problem",
                                        val httpStatus: HttpStatus = HttpStatus.OK,
                                        val scpiStatus: ScpiStatus = ScpiStatus.HUB_CONNECTION_PROBLEM): Exception(message)
