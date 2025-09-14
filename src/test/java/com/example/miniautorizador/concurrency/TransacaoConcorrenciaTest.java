package com.example.miniautorizador.concurrency;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransacaoConcorrenciaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartaoRepository cartaoRepository;

    private static final String NUMERO_CARTAO = "1234567890123456";
    private static final String SENHA = "1234";

    @BeforeEach
    public void setup() {
        cartaoRepository.deleteAll();
    }

    @Test
    public void testTransacoesConcorrentesNoMesmoCartao() throws Exception {
        CartaoDto cartaoDto = new CartaoDto(NUMERO_CARTAO, SENHA);
        mockMvc.perform(post("/cartoes")
                .with(httpBasic("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartaoDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/cartoes/" + NUMERO_CARTAO)
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk());

        TransacaoDto transacao1 = new TransacaoDto(NUMERO_CARTAO, SENHA, new BigDecimal("300.00"));
        TransacaoDto transacao2 = new TransacaoDto(NUMERO_CARTAO, SENHA, new BigDecimal("300.00"));

        int numThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);

        List<Future<Boolean>> futures = new ArrayList<>();
        // transacao 1 iniciando
        futures.add(executorService.submit(() -> {
            try {
                latch.await();
                System.out.println("[DEBUG_LOG] Iniciando transação 1");

                MvcResult result = mockMvc.perform(post("/transacoes")
                        .with(httpBasic("user", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacao1)))
                        .andReturn();

                int status = result.getResponse().getStatus();
                String content = result.getResponse().getContentAsString();
                System.out.println("[DEBUG_LOG] Transação 1 - Status: " + status + ", Conteúdo: " + content);

                if (status == 201 && "OK".equals(content)) {
                    sucessos.incrementAndGet();
                    return true;
                } else {
                    falhas.incrementAndGet();
                    return false;
                }
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] Erro na transação 1: " + e.getMessage());
                falhas.incrementAndGet();
                return false;
            }
        }));

        // Segunda transação
        futures.add(executorService.submit(() -> {
            try {
                latch.await();
                System.out.println("[DEBUG_LOG] Iniciando transação 2");

                MvcResult result = mockMvc.perform(post("/transacoes")
                        .with(httpBasic("user", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacao2)))
                        .andReturn();

                int status = result.getResponse().getStatus();
                String content = result.getResponse().getContentAsString();
                System.out.println("[DEBUG_LOG] Transação 2 - Status: " + status + ", Conteúdo: " + content);

                if (status == 201 && "OK".equals(content)) {
                    sucessos.incrementAndGet();
                    return true;
                } else {
                    falhas.incrementAndGet();
                    return false;
                }
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] Erro na transação 2: " + e.getMessage());
                falhas.incrementAndGet();
                return false;
            }
        }));

        // Iniciar as transações concorrentemente
        latch.countDown();

        for (Future<Boolean> future : futures) {
            future.get();
        }

        executorService.shutdown();

        MvcResult result = mockMvc.perform(get("/cartoes/" + NUMERO_CARTAO)
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andReturn();

        String saldoFinal = result.getResponse().getContentAsString();
        System.out.println("[DEBUG_LOG] Saldo final: " + saldoFinal);

        System.out.println("[DEBUG_LOG] Transações bem-sucedidas: " + sucessos.get());
        System.out.println("[DEBUG_LOG] Transações falhas: " + falhas.get());
        System.out.println("[DEBUG_LOG] Saldo final esperado se apenas uma transação for autorizada: 200.00");
        System.out.println("[DEBUG_LOG] Saldo final real: " + saldoFinal);
    }
}
