//package snc.smartchargingnetwork.node.services
//
//import io.mockk.every
//import io.mockk.mockk
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.Test
//import snc.smartchargingnetwork.node.data.examplePlatforms
//import snc.smartchargingnetwork.node.data.exampleRoles
//import snc.smartchargingnetwork.node.models.scpi.ConnectionStatus
//import snc.smartchargingnetwork.node.repositories.PlatformRepository
//import snc.smartchargingnetwork.node.repositories.RoleRepository
//
//class HubClientInfoServiceTest {
//
//    private val platformRepo: PlatformRepository = mockk()
//    private val roleRepo: RoleRepository = mockk()
//
//    private val hubClientInfoService: HubClientInfoService
//
//    init {
//        hubClientInfoService = HubClientInfoService(platformRepo, roleRepo)
//    }
//
//    @Test
//    fun getLocalList() {
//        every { platformRepo.findAll() } returns examplePlatforms.asIterable()
//        every { roleRepo.findAllByPlatformID(1L) } returns exampleRoles.filter { it.platformID == 1L }
//        every { roleRepo.findAllByPlatformID(2L) } returns exampleRoles.filter { it.platformID == 2L }
//        every { roleRepo.findAllByPlatformID(3L) } returns exampleRoles.filter { it.platformID == 3L }
//        val localList = hubClientInfoService.getLocalList()
//        assertThat(localList.size).isEqualTo(exampleRoles.size)
//        assertThat(localList.filter { it.status == ConnectionStatus.CONNECTED }.size).isEqualTo(3)
//    }
//
//}
