package com.example.prog1learnapp.service.lsp;

import com.example.prog1learnapp.config.lsp.LspProperties;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdtLsContainerServiceSaturationTest {

    @Test
    void acquireContainer_whenAtMaxSessions_rejectsAndIncrementsSaturationMetrics() throws Exception {
        LspProperties properties = new LspProperties();
        properties.setEnabled(true);
        properties.setMaxSessions(1);
        JdtLsContainerService service = new JdtLsContainerService(properties);

        setField(service, "dockerAvailable", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> sessions = (Map<String, Object>) getField(service, "sessions");
        sessions.put("existing-session", newContainerSession("container-existing", 1, System.currentTimeMillis()));

        Optional<String> result = service.acquireContainer("new-session");
        JdtLsContainerService.SaturationSnapshot snapshot = service.getSaturationSnapshot();

        assertTrue(result.isEmpty());
        assertEquals(1, snapshot.getAcquireAttempts());
        assertEquals(1, snapshot.getAcquireFailureCount());
        assertEquals(1, snapshot.getSaturationRejectCount());
        assertEquals("new-session", snapshot.getLastSaturationSessionKey());
    }

    @Test
    void cleanupIdleContainers_doesNotRemoveActiveSessions() throws Exception {
        LspProperties properties = new LspProperties();
        properties.setEnabled(true);
        properties.setIdleTtlSeconds(0);
        JdtLsContainerService service = new JdtLsContainerService(properties);

        setField(service, "dockerAvailable", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> sessions = (Map<String, Object>) getField(service, "sessions");
        sessions.put("active-session", newContainerSession("container-active", 1, 0L));

        service.cleanupIdleContainers();

        @SuppressWarnings("unchecked")
        Map<String, Object> remaining = (Map<String, Object>) getField(service, "sessions");
        assertEquals(1, remaining.size());
        assertTrue(remaining.containsKey("active-session"));
    }

    @Test
    void acquireContainer_usesIdlePoolBeforeCreatingNewContainer() throws Exception {
        LspProperties properties = new LspProperties();
        properties.setEnabled(true);
        properties.setMaxSessions(5);
        JdtLsContainerService service = new JdtLsContainerService(properties);

        setField(service, "dockerAvailable", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> idlePool = (Map<String, Object>) getField(service, "idleContainers");
        idlePool.put("proginator-jdtls-idle-pool-a", newIdleContainer("proginator-jdtls-idle-pool-a", System.currentTimeMillis()));

        Optional<String> container = service.acquireContainer("session-a");
        JdtLsContainerService.SaturationSnapshot snapshot = service.getSaturationSnapshot();

        assertTrue(container.isPresent());
        assertEquals("proginator-jdtls-idle-pool-a", container.get());
        assertEquals(1, snapshot.getAcquireIdlePoolHitCount());
        assertEquals(0, snapshot.getAcquireCreateCount());
    }

    private Object newContainerSession(String containerName, int refCount, long lastUsedEpochMs) throws Exception {
        Class<?> containerSessionClass = Class.forName(
                "com.example.prog1learnapp.service.lsp.JdtLsContainerService$ContainerSession"
        );
        Constructor<?> constructor = containerSessionClass.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        Object session = constructor.newInstance(containerName);
        setField(session, "refCount", refCount);
        setField(session, "lastUsedEpochMs", lastUsedEpochMs);
        return session;
    }

    private Object newIdleContainer(String containerName, long lastUsedEpochMs) throws Exception {
        Class<?> idleContainerClass = Class.forName(
                "com.example.prog1learnapp.service.lsp.JdtLsContainerService$IdleContainer"
        );
        Constructor<?> constructor = idleContainerClass.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        Object idle = constructor.newInstance(containerName);
        setField(idle, "lastUsedEpochMs", lastUsedEpochMs);
        return idle;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getField(Object target, String fieldName) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
