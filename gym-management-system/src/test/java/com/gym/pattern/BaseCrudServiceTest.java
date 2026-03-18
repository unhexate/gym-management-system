package com.gym.pattern;

import com.gym.service.BaseCrudService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Template Method Pattern (BaseCrudService).
 * Uses a lightweight in-memory stub subclass – no Spring context.
 */
@DisplayName("BaseCrudService – Template Method Pattern")
class BaseCrudServiceTest {

    // -----------------------------------------------------------------
    // Minimal stub that records which hooks were called and in what order
    // -----------------------------------------------------------------

    static class StubService extends BaseCrudService<String, Integer> {

        final List<String> callOrder = new ArrayList<>();
        private String stored;

        @Override
        protected String save(String entity) {
            callOrder.add("save");
            stored = entity;
            return entity;
        }

        @Override
        protected String findById(Integer id) {
            callOrder.add("findById");
            if (stored == null) throw new RuntimeException("not found");
            return stored;
        }

        @Override
        protected void performDelete(Integer id) {
            callOrder.add("performDelete");
            stored = null;
        }

        @Override
        protected void validate(String entity) { callOrder.add("validate"); }

        @Override
        protected void beforeSave(String entity) { callOrder.add("beforeSave"); }

        @Override
        protected void afterSave(String saved) { callOrder.add("afterSave"); }

        @Override
        protected void beforeDelete(String entity) { callOrder.add("beforeDelete"); }

        @Override
        protected void afterDelete(String deleted) { callOrder.add("afterDelete"); }
    }

    @Test
    @DisplayName("create() calls hooks in correct order: validate → beforeSave → save → afterSave")
    void createCallsHooksInOrder() {
        StubService svc = new StubService();
        svc.create("testEntity");
        assertEquals(List.of("validate", "beforeSave", "save", "afterSave"), svc.callOrder);
    }

    @Test
    @DisplayName("create() returns the saved entity")
    void createReturnsSavedEntity() {
        StubService svc = new StubService();
        String result = svc.create("hello");
        assertEquals("hello", result);
    }

    @Test
    @DisplayName("delete() calls hooks in correct order: findById → beforeDelete → performDelete → afterDelete")
    void deleteCallsHooksInOrder() {
        StubService svc = new StubService();
        svc.create("toDelete"); // populate stored
        svc.callOrder.clear();

        svc.delete(1);
        assertEquals(List.of("findById", "beforeDelete", "performDelete", "afterDelete"), svc.callOrder);
    }
}
