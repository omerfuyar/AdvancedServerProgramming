package com.dsu.hello_server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// The whole application through MockMvc: security filters, controllers, ApiExceptionHandler, MySQL.
// Needs MySQL running with the reset.sql data (mina owns reservation 1).
// @Transactional: every test is rolled back, so the database stays as reset.sql left it.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiFailureTest {

    @Autowired
    private MockMvc mvc;

    // class passwords: name + 1234
    private static String basic(String username) {
        String pair = username + ":" + username + "1234";
        return "Basic " + Base64.getEncoder()
                .encodeToString(pair.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void noLoginIs401() throws Exception {
        mvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("login required"));
    }

    @Test
    void userCreatingRoomIs403() throws Exception { // rule D
        mvc.perform(post("/api/rooms")
                        .header("Authorization", basic("mina"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Test Room", "capacity": 5 }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("admin role required"));
    }

    @Test
    void adminCreatingRoomIs201() throws Exception {
        mvc.perform(post("/api/rooms")
                        .header("Authorization", basic("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Test Room", "capacity": 5 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Test Room"));
    }

    // ---------- Lab H2: the seven failures of R7 (4 and 5 are above) ----------

    @Test
    void overlapIs409() throws Exception { // rule A
        mvc.perform(post("/api/rooms/1/reservations")
                        .header("Authorization", basic("omar"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "startTime": "2026-10-06T10:30:00", "endTime": "2026-10-06T11:30:00" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Room 1 is already reserved at that time"));
    }

    @Test
    void endBeforeStartIs400() throws Exception { // rule B
        mvc.perform(post("/api/rooms/1/reservations")
                        .header("Authorization", basic("mina"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "startTime": "2026-12-01T11:00:00", "endTime": "2026-12-01T10:00:00" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("startTime must be before endTime"));
    }

    @Test
    void anotherUsersReservationIs403() throws Exception { // rule C
        mvc.perform(get("/api/reservations/1")
                        .header("Authorization", basic("omar")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("reservation 1 belongs to another user"));
    }

    @Test
    void deleteRoomWithReservationsIs409() throws Exception { // P5 children
        mvc.perform(delete("/api/rooms/1")
                        .header("Authorization", basic("admin")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Room 1 has reservations"));
    }

    @Test
    void missingFieldIs400() throws Exception { // validation
        mvc.perform(post("/api/rooms/1/reservations")
                        .header("Authorization", basic("mina"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "startTime": "2026-12-01T10:00:00" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("startTime and endTime are required"));
    }

    // ---------- Done early ----------

    @Test
    void wrongPasswordIs401() throws Exception {
        String wrong = "Basic " + Base64.getEncoder()
                .encodeToString("mina:wrong".getBytes(StandardCharsets.UTF_8));
        mvc.perform(get("/api/reservations/mine")
                        .header("Authorization", wrong))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("wrong username or password"));
    }

    @Test
    void missingReservationIs404() throws Exception {
        mvc.perform(get("/api/reservations/99")
                        .header("Authorization", basic("omar")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reservation 99 not found"));
    }
}
