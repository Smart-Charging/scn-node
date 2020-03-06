package snc.smartchargingnetwork.node.data

import snc.smartchargingnetwork.node.models.entities.Auth
import snc.smartchargingnetwork.node.models.entities.PlatformEntity
import snc.smartchargingnetwork.node.models.entities.RoleEntity
import snc.smartchargingnetwork.node.models.scpi.BusinessDetails
import snc.smartchargingnetwork.node.models.scpi.ConnectionStatus
import snc.smartchargingnetwork.node.models.scpi.Role

val examplePlatforms = arrayOf(
        PlatformEntity(
                status = ConnectionStatus.CONNECTED,
                auth = Auth(),
                id = 1L),
        PlatformEntity(
                status = ConnectionStatus.OFFLINE,
                auth = Auth(),
                id = 2L),
        PlatformEntity(
                status = ConnectionStatus.CONNECTED,
                auth = Auth(),
                id = 3L))

val exampleRoles = arrayOf(
        RoleEntity(
                platformID = 1L,
                role = Role.CPO,
                businessDetails = BusinessDetails(name = "Great CPO Ltd."),
                countryCode = "UK",
                partyID = "GRE"),
        RoleEntity(
                platformID = 1L,
                role = Role.NSP,
                businessDetails = BusinessDetails(name = "Great NSP Ltd."),
                countryCode = "UK",
                partyID = "GRE"),
        RoleEntity(
                platformID = 2L,
                role = Role.EMSP,
                businessDetails = BusinessDetails(name = "Cool EMSP GmbH"),
                countryCode = "DE",
                partyID = "CEM"),
        RoleEntity(
                platformID = 3L,
                role = Role.CPO,
                businessDetails = BusinessDetails(name = "Cooler CPO GmbH"),
                countryCode = "DE",
                partyID = "CCO"))
