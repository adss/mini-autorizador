package com.example.miniautorizador.controller;

import com.example.miniautorizador.dto.CartaoDto;
import com.example.miniautorizador.dto.TransacaoDto;
import com.example.miniautorizador.repository.CartaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CartaoControllerIntegrationTest {

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
    public void testCriarCartao() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(cartaoDto)));
    }

    @Test
    public void testCriarCartaoDuplicado() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(objectMapper.writeValueAsString(cartaoDto)));
    }

    @Test
    public void testObterSaldo() throws Exception {
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
    public void testObterSaldoCartaoInexistente() throws Exception {
        mockMvc.perform(get("/cartoes/9999999999999999")
                .with(httpBasic("user", "password")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRealizarTransacaoComSucesso() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "1234", new BigDecimal("100.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("400.00"));
    }

    @Test
    public void testRealizarTransacaoComSaldoInsuficiente() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        TransacaoDto transacaoDto1 = new TransacaoDto("1234567890123456", "1234", new BigDecimal("500.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto1)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        TransacaoDto transacaoDto2 = new TransacaoDto("1234567890123456", "1234", new BigDecimal("100.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto2)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));
    }

    @Test
    public void testRealizarTransacaoComSenhaInvalida() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");

        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "4321", new BigDecimal("100.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));
    }

    @Test
    public void testRealizarTransacaoComCartaoInexistente() throws Exception {
        TransacaoDto transacaoDto = new TransacaoDto("9999999999999999", "1234", new BigDecimal("100.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));
    }
}
