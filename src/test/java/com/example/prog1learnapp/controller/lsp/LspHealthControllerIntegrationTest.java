package com.example.prog1learnapp.controller.lsp;

import com.example.prog1learnapp.service.lsp.JdtLsContainerService;
import com.example.prog1learnapp.service.lsp.LspSessionManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LspHealthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdtLsContainerService containerService;

    @MockBean
    private LspSessionManager sessionManager;

    @Test
    @WithMockUser(username = "health-user")
    void health_returns200WhenLspInfrastructureIsHealthy() throws Exception {
        when(containerService.isEnabled()).thenReturn(true);
        when(containerService.isDockerAvailable()).thenReturn(true);
        when(containerService.isImageAvailable()).thenReturn(true);
        when(containerService.getActiveSessionCount()).thenReturn(2);
        when(containerService.getSaturationSnapshot()).thenReturn(new JdtLsContainerService.SaturationSnapshot(
                10, 6, 3, 1, 0, 2, 20, 0, null
        ));
        when(sessionManager.getActiveBridgeCount()).thenReturn(2);

        mockMvc.perform(get("/api/lsp/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.dockerAvailable").value(true))
                .andExpect(jsonPath("$.imageAvailable").value(true))
                .andExpect(jsonPath("$.activeContainers").value(2))
                .andExpect(jsonPath("$.activeBridges").value(2))
                .andExpect(jsonPath("$.saturation.acquireIdlePoolHitCount").value(0))
                .andExpect(jsonPath("$.saturation.idlePoolSize").value(0))
                .andExpect(jsonPath("$.saturation.maxSessions").value(20))
                .andExpect(jsonPath("$.saturation.saturationRejectCount").value(0));
    }

    @Test
    @WithMockUser(username = "health-user")
    void health_returns503WhenInfrastructureIsNotReady() throws Exception {
        when(containerService.isEnabled()).thenReturn(true);
        when(containerService.isDockerAvailable()).thenReturn(false);
        when(containerService.isImageAvailable()).thenReturn(false);
        when(containerService.getActiveSessionCount()).thenReturn(0);
        when(containerService.getSaturationSnapshot()).thenReturn(new JdtLsContainerService.SaturationSnapshot(
                2, 0, 0, 2, 1, 0, 1, 1739980800000L, "alice:http-2"
        ));
        when(sessionManager.getActiveBridgeCount()).thenReturn(0);

        mockMvc.perform(get("/api/lsp/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.dockerAvailable").value(false))
                .andExpect(jsonPath("$.imageAvailable").value(false))
                .andExpect(jsonPath("$.activeContainers").value(0))
                .andExpect(jsonPath("$.activeBridges").value(0))
                .andExpect(jsonPath("$.saturation.saturationRejectCount").value(1))
                .andExpect(jsonPath("$.saturation.lastSaturationSessionKey").value("alice:http-2"));
    }
}
