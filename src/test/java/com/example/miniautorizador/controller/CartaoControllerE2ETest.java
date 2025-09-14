package com.example.miniautorizador.controller;

import com.example.miniautorizador.dto.CartaoDto;
import com.example.miniautorizador.repository.CartaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CartaoControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartaoRepository cartaoRepository;

    @BeforeEach
    public void setup() {
        cartaoRepository.deleteAll();
    }

    @Test
    public void testCriarCartaoE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(cartaoDto)));
    }

    @Test
    public void testCriarCartaoDuplicadoE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        // tentativa de criação duplicada
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(objectMapper.writeValueAsString(cartaoDto)));
    }

    @Test
    public void testObterSaldoE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    public void testObterSaldoCartaoInexistenteE2E() throws Exception {
        mockMvc.perform(get("/cartoes/9999999999999999")
                .with(httpBasic("user", "password")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testFluxoCompletoCartaoE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("9876543210987654", "4321");

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(cartaoDto)));

        mockMvc.perform(get("/cartoes/9876543210987654")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(objectMapper.writeValueAsString(cartaoDto)));
    }
}