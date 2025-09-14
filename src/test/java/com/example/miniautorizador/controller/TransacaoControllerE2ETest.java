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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TransacaoControllerE2ETest {

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
    public void testRealizarTransacaoComSucessoE2E() throws Exception {
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
    public void testRealizarTransacaoComSaldoInsuficienteE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "1234", new BigDecimal("600.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));

        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    public void testRealizarTransacaoComSenhaInvalidaE2E() throws Exception {
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

        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    public void testRealizarTransacaoComCartaoInexistenteE2E() throws Exception {
        TransacaoDto transacaoDto = new TransacaoDto("9999999999999999", "1234", new BigDecimal("100.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));
    }

    @Test
    public void testFluxoCompletoTransacaoE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("9876543210987654", "4321");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/cartoes/9876543210987654")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));

        TransacaoDto transacaoDto1 = new TransacaoDto("9876543210987654", "4321", new BigDecimal("200.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto1)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        mockMvc.perform(get("/cartoes/9876543210987654")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("300.00"));

        TransacaoDto transacaoDto2 = new TransacaoDto("9876543210987654", "4321", new BigDecimal("300.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto2)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        mockMvc.perform(get("/cartoes/9876543210987654")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("0.00"));

        TransacaoDto transacaoDto3 = new TransacaoDto("9876543210987654", "4321", new BigDecimal("0.01"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto3)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));
    }

    @Test
    public void testRealizarTransacaoComValorZeroE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "1234", new BigDecimal("0.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("VALOR_INVALIDO"));

        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    public void testRealizarTransacaoComValorNegativoE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "1234", new BigDecimal("-10.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("VALOR_INVALIDO"));

        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    public void testRealizarTransacaoComValorNuloE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "1234", null);
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("VALOR_INVALIDO"));

        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    public void testRealizarTransacaoComSenhaNulaE2E() throws Exception {
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", null, new BigDecimal("100.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));

        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }
}
