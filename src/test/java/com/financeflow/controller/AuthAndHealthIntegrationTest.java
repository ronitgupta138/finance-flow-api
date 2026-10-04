package com.financeflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeflow.dto.AuthRequest;
import com.financeflow.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthAndHealthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("FinanceFlow-API"));
    }

    @Test
    void testRegisterAndLoginFlow() throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setEmail("alex.dev@test.io");
        regReq.setPassword("Password123!");
        regReq.setFullName("Alex Developer");

        // 1. Register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.email").value("alex.dev@test.io"));

        // 2. Login
        AuthRequest authReq = new AuthRequest();
        authReq.setEmail("alex.dev@test.io");
        authReq.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists());

        // 3. Login with wrong password -> Expect 401 Unauthorized
        AuthRequest badAuth = new AuthRequest();
        badAuth.setEmail("alex.dev@test.io");
        badAuth.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badAuth)))
                .andExpect(status().isUnauthorized());
    }
}
