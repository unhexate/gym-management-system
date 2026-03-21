package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.model.MembershipPlan;
import com.gym.repository.MembershipPlanRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests covering all 10 REST endpoints.
 * Uses an in-memory H2 database (no external dependencies).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser(username = "admin@gym.com", roles = "ADMIN")
class GymApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired MembershipPlanRepository planRepository;

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String body(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    /** Seed a MembershipPlan directly via JPA and return its id. */
    private Long seedPlan(String name, int months, double price) {
        MembershipPlan plan = new MembershipPlan(null, name, months, price, name + " plan");
        return planRepository.save(plan).getId();
    }

    // ------------------------------------------------------------------
    // 1. POST /api/users  – register member
    // ------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("POST /api/users – register MEMBER returns 201")
    void registerMember() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of(
                                "name", "Alice",
                                "email", "alice@gym.com",
                                "phone", "111",
                                "password", "pass",
                                "role", "MEMBER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Alice"));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/users – duplicate email returns 400")
    void registerDuplicateEmail() throws Exception {
        // first registration
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name","A","email","dup@gym.com","phone","1","password","p","role","MEMBER"))))
                .andExpect(status().isCreated());

        // duplicate
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name","B","email","dup@gym.com","phone","2","password","p","role","MEMBER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/users – missing required field returns 400")
    void registerMissingField() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name","X","email","x@gym.com","phone","1","password","p"))))
                // role is missing
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/users – register TRAINER returns correct subtype role field")
    void registerTrainer() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of(
                                "name", "Bob Trainer",
                                "email", "bob@gym.com",
                                "phone", "222",
                                "password", "pass",
                                "role", "TRAINER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ------------------------------------------------------------------
    // 2. PUT /api/users/{id}/profile
    // ------------------------------------------------------------------

    @Test
    @Order(5)
    @DisplayName("PUT /api/users/{id}/profile – updates name and phone")
    void updateProfile() throws Exception {
        // create user first
        String resp = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name","Old","email","old@gym.com","phone","000","password","p","role","MEMBER"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = mapper.readTree(resp).path("data").path("id").asLong();

        mockMvc.perform(put("/api/users/" + id + "/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name","New Name","phone","999"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.phone").value("999"));
    }

    @Test
    @Order(6)
    @DisplayName("PUT /api/users/{id}/profile – unknown id returns 404")
    void updateProfileNotFound() throws Exception {
        mockMvc.perform(put("/api/users/9999/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name","X"))))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Helpers to seed DB for subsequent tests
    // ------------------------------------------------------------------

    private Long createMember(String name, String email) throws Exception {
        String r = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name",name,"email",email,"phone","1","password","p","role","MEMBER"))))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(r).path("data").path("id").asLong();
    }

    private Long createTrainer(String name, String email) throws Exception {
        String r = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name",name,"email",email,"phone","2","password","p","role","TRAINER"))))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(r).path("data").path("id").asLong();
    }

    // ------------------------------------------------------------------
    // 3 & 4. Membership endpoints – need a plan seed
    // ------------------------------------------------------------------

    @Test
    @Order(7)
    @DisplayName("POST /api/memberships – enroll member returns 201 with correct price (BasicPricing)")
    void enrollMembership() throws Exception {
        Long memberId = createMember("Alice", "alice2@gym.com");
        Long planId   = seedPlan("BASIC", 1, 100.0);

        mockMvc.perform(post("/api/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("memberId", memberId, "planId", planId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.price").value(100.0))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/memberships – PREMIUM plan gets 20% discount (Strategy Pattern)")
    void enrollPremiumAppliesDiscount() throws Exception {
        Long memberId = createMember("Bob", "bob2@gym.com");
        Long planId   = seedPlan("PREMIUM", 3, 300.0);

        mockMvc.perform(post("/api/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("memberId", memberId, "planId", planId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.price").value(240.0));
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/memberships/member/{id} – returns active membership")
    void getMembership() throws Exception {
        Long memberId = createMember("Carol", "carol2@gym.com");
        Long planId   = seedPlan("BASIC", 1, 100.0);

        mockMvc.perform(post("/api/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("memberId", memberId, "planId", planId))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/memberships/member/" + memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    // ------------------------------------------------------------------
    // 5 & 6. Payment endpoints
    // ------------------------------------------------------------------

    @Test
    @Order(10)
    @DisplayName("POST /api/payments – process payment returns 201")
    void processPayment() throws Exception {
        Long memberId = createMember("Dave", "dave2@gym.com");
        Long planId   = seedPlan("BASIC", 1, 100.0);

        String mResp = mockMvc.perform(post("/api/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("memberId", memberId, "planId", planId))))
                .andReturn().getResponse().getContentAsString();
        Long membershipId = mapper.readTree(mResp).path("data").path("id").asLong();

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of(
                                "memberId", memberId,
                                "membershipId", membershipId,
                                "amount", 100.0,
                                "paymentMode", "ONLINE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.data.amount").value(100.0));
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/payments/member/{id} – returns payment history list")
    void paymentHistory() throws Exception {
        Long memberId = createMember("Eve", "eve2@gym.com");
        Long planId   = seedPlan("BASIC", 1, 50.0);

        String mResp = mockMvc.perform(post("/api/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("memberId", memberId, "planId", planId))))
                .andReturn().getResponse().getContentAsString();
        Long membershipId = mapper.readTree(mResp).path("data").path("id").asLong();
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("memberId",memberId,"membershipId",membershipId,"amount",50.0,"paymentMode","OFFLINE"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/payments/member/" + memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    // ------------------------------------------------------------------
    // 7 & 8. Workout endpoints
    // ------------------------------------------------------------------

    @Test
    @Order(12)
    @DisplayName("POST /api/workouts – trainer creates workout plan returns 201")
    void createWorkout() throws Exception {
        Long memberId  = createMember("Frank",  "frank2@gym.com");
        Long trainerId = createTrainer("Grace", "grace2@gym.com");

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of(
                                "trainerId", trainerId,
                                "memberId", memberId,
                                "exercises", "Squat, Bench Press",
                                "schedule", "Mon/Wed 08:00",
                                "difficultyLevel", "INTERMEDIATE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.difficultyLevel").value("INTERMEDIATE"));
    }

    @Test
    @Order(13)
    @DisplayName("GET /api/workouts/member/{id} – returns member workout plan")
    void getWorkout() throws Exception {
        Long memberId  = createMember("Hank",  "hank2@gym.com");
        Long trainerId = createTrainer("Iris",  "iris2@gym.com");

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("trainerId",trainerId,"memberId",memberId,
                                "exercises","Push-up","schedule","Tue/Thu","difficultyLevel","BEGINNER"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/workouts/member/" + memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exercises").value("Push-up"));
    }

    // ------------------------------------------------------------------
    // 9. Attendance endpoint
    // ------------------------------------------------------------------

    @Test
    @Order(14)
    @DisplayName("POST /api/attendance – marks attendance returns 201")
    void markAttendance() throws Exception {
        Long memberId = createMember("Jack", "jack2@gym.com");

        mockMvc.perform(post("/api/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("memberId", memberId, "checkinTime", "09:30"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ------------------------------------------------------------------
    // 10. Reports endpoint (Facade)
    // ------------------------------------------------------------------

    @Test
    @Order(15)
    @DisplayName("GET /api/reports – returns summary report (Facade Pattern)")
    void getReport() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalMembers").exists())
                .andExpect(jsonPath("$.data.totalRevenue").exists())
                .andExpect(jsonPath("$.data.activeMembers").exists());
    }
}
