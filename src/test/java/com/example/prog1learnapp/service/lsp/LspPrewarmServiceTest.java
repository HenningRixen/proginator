package com.example.prog1learnapp.service.lsp;

import com.example.prog1learnapp.config.lsp.LspProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LspPrewarmServiceTest {

    @Test
    void scheduleLoginPrewarm_disabledFlag_noCall() throws Exception {
        LspProperties properties = new LspProperties();
        properties.setEnabled(true);
        properties.setPrewarmOnLogin(false);
        LspSessionManager manager = Mockito.mock(LspSessionManager.class);
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        LspPrewarmService service = new LspPrewarmService(properties, manager, containerService, Runnable::run);

        boolean scheduled = service.scheduleLoginPrewarm("alice", "http-1");

        assertFalse(scheduled);
        verify(manager, never()).prewarmWorkspace(Mockito.anyString());
    }

    @Test
    void scheduleLoginPrewarm_enabled_inFlightGuardAllowsOnlyOne() throws Exception {
        LspProperties properties = new LspProperties();
        properties.setEnabled(true);
        properties.setPrewarmOnLogin(true);
        properties.setPrewarmCooldownSeconds(0);
        LspSessionManager manager = Mockito.mock(LspSessionManager.class);
        when(manager.workspaceKeyForSession("alice", "http-1")).thenReturn("alice:http-1");
        when(manager.hasBridgeForWorkspaceKey("alice:http-1")).thenReturn(false);
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.getSaturationSnapshot()).thenReturn(new JdtLsContainerService.SaturationSnapshot(
                1, 0, 0, 0, 0, 0, 20, 0, null
        ));

        ManualExecutor executor = new ManualExecutor();
        LspPrewarmService service = new LspPrewarmService(properties, manager, containerService, executor);

        boolean first = service.scheduleLoginPrewarm("alice", "http-1");
        boolean second = service.scheduleLoginPrewarm("alice", "http-1");
        executor.runAll();

        assertTrue(first);
        assertFalse(second);
        verify(manager, times(1)).prewarmWorkspace("alice:http-1");
    }

    @Test
    void scheduleLoginPrewarm_existingBackend_skips() throws Exception {
        LspProperties properties = new LspProperties();
        properties.setEnabled(true);
        properties.setPrewarmOnLogin(true);
        properties.setPrewarmCooldownSeconds(0);
        LspSessionManager manager = Mockito.mock(LspSessionManager.class);
        when(manager.workspaceKeyForSession("alice", "http-1")).thenReturn("alice:http-1");
        when(manager.hasBridgeForWorkspaceKey("alice:http-1")).thenReturn(true);
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.getSaturationSnapshot()).thenReturn(new JdtLsContainerService.SaturationSnapshot(
                1, 0, 0, 0, 0, 0, 20, 0, null
        ));

        LspPrewarmService service = new LspPrewarmService(properties, manager, containerService, Runnable::run);
        boolean scheduled = service.scheduleLoginPrewarm("alice", "http-1");

        assertFalse(scheduled);
        verify(manager, never()).prewarmWorkspace(Mockito.anyString());
    }

    @Test
    void scheduleLoginPrewarm_failurePath_doesNotThrow() throws Exception {
        LspProperties properties = new LspProperties();
        properties.setEnabled(true);
        properties.setPrewarmOnLogin(true);
        properties.setPrewarmCooldownSeconds(0);
        LspSessionManager manager = Mockito.mock(LspSessionManager.class);
        when(manager.workspaceKeyForSession("alice", "http-1")).thenReturn("alice:http-1");
        when(manager.hasBridgeForWorkspaceKey("alice:http-1")).thenReturn(false);
        Mockito.doThrow(new IOException("saturation")).when(manager).prewarmWorkspace("alice:http-1");
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.getSaturationSnapshot()).thenReturn(new JdtLsContainerService.SaturationSnapshot(
                1, 0, 0, 0, 0, 0, 20, 0, null
        ));

        LspPrewarmService service = new LspPrewarmService(properties, manager, containerService, Runnable::run);
        boolean scheduled = service.scheduleLoginPrewarm("alice", "http-1");

        assertTrue(scheduled);
        verify(manager, times(1)).prewarmWorkspace("alice:http-1");
    }

    @Test
    void scheduleLoginPrewarm_cooldownSkipsRepeatedSchedule() throws Exception {
        LspProperties properties = new LspProperties();
        properties.setEnabled(true);
        properties.setPrewarmOnLogin(true);
        properties.setPrewarmCooldownSeconds(60);
        LspSessionManager manager = Mockito.mock(LspSessionManager.class);
        when(manager.workspaceKeyForSession("alice", "http-1")).thenReturn("alice:http-1");
        when(manager.hasBridgeForWorkspaceKey("alice:http-1")).thenReturn(false);
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.getSaturationSnapshot()).thenReturn(new JdtLsContainerService.SaturationSnapshot(
                1, 0, 0, 0, 0, 0, 20, 0, null
        ));

        LspPrewarmService service = new LspPrewarmService(properties, manager, containerService, Runnable::run);

        boolean first = service.scheduleLoginPrewarm("alice", "http-1");
        boolean second = service.scheduleLoginPrewarm("alice", "http-1");

        assertTrue(first);
        assertFalse(second);
        verify(manager, times(1)).prewarmWorkspace("alice:http-1");
    }

    @Test
    void scheduleLoginPrewarm_nearSaturationSkips() throws Exception {
        LspProperties properties = new LspProperties();
        properties.setEnabled(true);
        properties.setPrewarmOnLogin(true);
        properties.setPrewarmCooldownSeconds(0);
        properties.setPrewarmSkipSaturationPercent(90);
        LspSessionManager manager = Mockito.mock(LspSessionManager.class);
        when(manager.workspaceKeyForSession("alice", "http-1")).thenReturn("alice:http-1");
        when(manager.hasBridgeForWorkspaceKey("alice:http-1")).thenReturn(false);
        JdtLsContainerService containerService = Mockito.mock(JdtLsContainerService.class);
        when(containerService.getSaturationSnapshot()).thenReturn(new JdtLsContainerService.SaturationSnapshot(
                10, 2, 8, 0, 0, 18, 20, 0, null
        ));

        LspPrewarmService service = new LspPrewarmService(properties, manager, containerService, Runnable::run);
        boolean scheduled = service.scheduleLoginPrewarm("alice", "http-1");

        assertFalse(scheduled);
        verify(manager, never()).prewarmWorkspace(Mockito.anyString());
    }

    private static final class ManualExecutor implements Executor {
        private final Queue<Runnable> queue = new ArrayDeque<>();

        @Override
        public void execute(Runnable command) {
            queue.add(command);
        }

        private void runAll() {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }
}
