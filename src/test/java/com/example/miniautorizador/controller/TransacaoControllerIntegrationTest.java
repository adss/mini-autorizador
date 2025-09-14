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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransacaoControllerIntegrationTest {

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
    public void testTransacaoIgualAoSaldoDisponivel() throws Exception {
        // Criar cartão
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        // Verificar saldo inicial
        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));

        // Realizar transação com valor exatamente igual ao saldo
        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "1234", new BigDecimal("500.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        // Verificar que o saldo foi zerado
        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("0.00"));

        // realizar nova transação, que deve falhar por saldo insuficiente
        TransacaoDto novaTransacaoDto = new TransacaoDto("1234567890123456", "1234", new BigDecimal("0.01"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novaTransacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));
    }

    @Test
    public void testSenhaInvalidaRetornaErroEsperado() throws Exception {
        // Criar cartão
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        // realizar transação com senha inválida
        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "senha_errada", new BigDecimal("100.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));
    }

    @Test
    public void testCartaoInexistenteRetornaErroEsperado() throws Exception {
        // realizar transação com cartão inexistente
        TransacaoDto transacaoDto = new TransacaoDto("9999999999999999", "1234", new BigDecimal("100.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));
    }

    @Test
    public void testValoresInvalidosNoPayload() throws Exception {
        // Criar cartão
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        // Teste com valor zero
        TransacaoDto transacaoValorZero = new TransacaoDto("1234567890123456", "1234", new BigDecimal("0.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoValorZero)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("VALOR_INVALIDO"));

        // Teste com valor negativo
        TransacaoDto transacaoValorNegativo = new TransacaoDto("1234567890123456", "1234", new BigDecimal("-50.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoValorNegativo)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("VALOR_INVALIDO"));

        // Teste com número de cartão ausente
        String jsonSemNumeroCartao = "{\"senhaCartao\":\"1234\",\"valor\":100.00}";
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSemNumeroCartao))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));

        // Teste com senha ausente
        String jsonSemSenha = "{\"numeroCartao\":\"1234567890123456\",\"valor\":100.00}";
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSemSenha))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));

        // Teste com valor ausente
        String jsonSemValor = "{\"numeroCartao\":\"1234567890123456\",\"senhaCartao\":\"1234\"}";
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSemValor))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("VALOR_INVALIDO"));
    }

    @Test
    public void testValoresDecimaisComEscalaAlta() throws Exception {
        // Criar cartão
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        // realizar transação com valor com 3 casas decimais
        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "1234", new BigDecimal("100.005"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        // verificar que o saldo foi debitado corretamente
        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("400.00"));
    }

    @Test
    public void testCriacaoDuplicadaDeCartaoNaoAlteraSaldo() throws Exception {
        // Criar cartão
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        // realizar uma transação para alterar o saldo
        TransacaoDto transacaoDto = new TransacaoDto("1234567890123456", "1234", new BigDecimal("100.00"));
        mockMvc.perform(post("/transacoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
                .andExpect(status().isCreated());

        // verificar saldo após transação
        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("400.00"));

        // criar o mesmo cartão novamente
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(objectMapper.writeValueAsString(cartaoDto)));

        // vefica que o saldo não foi alterado
        mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("400.00"));
    }

    @Test
    public void testObterSaldoFormatoEStatusCorretos() throws Exception {
        // cria cartão
        CartaoDto cartaoDto = new CartaoDto("1234567890123456", "1234");
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        // vekrificar formato e status para cartão existente
        MvcResult result = mockMvc.perform(get("/cartoes/1234567890123456")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // verificar que o conteudo é um número decimal com 2 casas decimais
        assert content.matches("\\d+\\.\\d{2}");

        // verifica formato e status para cartão inexistente
        mockMvc.perform(get("/cartoes/9999999999999999")
                .with(httpBasic("user", "password")))
                .andExpect(status().isNotFound());
    }
}
