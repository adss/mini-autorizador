package com.example.miniautorizador.service;

import com.example.miniautorizador.dto.TransacaoDto;
import com.example.miniautorizador.enums.AutorizacaoErro;
import com.example.miniautorizador.exception.TransacaoNaoAutorizadaException;
import com.example.miniautorizador.model.Cartao;
import com.example.miniautorizador.repository.CartaoRepository;
import com.example.miniautorizador.service.authorization.AuthorizationChainFactory;
import com.example.miniautorizador.service.authorization.AuthorizationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransacaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private AuthorizationChainFactory authorizationChainFactory;

    @Mock
    private AuthorizationRule authorizationChain;

    @InjectMocks
    private TransacaoService transacaoService;

    private Cartao cartao;
    private TransacaoDto transacaoDto;

    @BeforeEach
    void setUp() {
        cartao = Cartao.builder()
                .id(1L)
                .numeroCartao("1234567890123456")
                .senha("1234")
                .saldo(new BigDecimal("500.00"))
                .build();

        transacaoDto = TransacaoDto.builder()
                .numeroCartao("1234567890123456")
                .senhaCartao("1234")
                .valor(new BigDecimal("100.00"))
                .build();

        when(authorizationChainFactory.createDefaultChain()).thenReturn(authorizationChain);
    }

    @Test
    void processarTransacao_Sucesso_DeveDebitarValorCorretamente() {
        when(cartaoRepository.findByNumeroCartao(transacaoDto.getNumeroCartao())).thenReturn(cartao);
        doNothing().when(authorizationChain).authorize(transacaoDto, cartao);

        transacaoService.processarTransacao(transacaoDto);

        verify(cartaoRepository).findByNumeroCartao(transacaoDto.getNumeroCartao());
        verify(authorizationChain).authorize(transacaoDto, cartao);
        verify(cartaoRepository).save(cartao);
        
        assertEquals(new BigDecimal("400.00").setScale(2, RoundingMode.HALF_EVEN), cartao.getSaldo());
    }

    @Test
    void processarTransacao_CartaoInexistente_DeveLancarExcecao() {
        when(cartaoRepository.findByNumeroCartao(transacaoDto.getNumeroCartao())).thenReturn(null);
        
        doThrow(new TransacaoNaoAutorizadaException("Cartão inexistente", AutorizacaoErro.CARTAO_INEXISTENTE))
            .when(authorizationChain).authorize(transacaoDto, null);

        TransacaoNaoAutorizadaException exception = assertThrows(
                TransacaoNaoAutorizadaException.class,
                () -> transacaoService.processarTransacao(transacaoDto)
        );

        verify(cartaoRepository).findByNumeroCartao(transacaoDto.getNumeroCartao());
        verify(authorizationChain).authorize(transacaoDto, null);
        verify(cartaoRepository, never()).save(any(Cartao.class));
        
        assertEquals("Cartão inexistente", exception.getMessage());
        assertEquals(AutorizacaoErro.CARTAO_INEXISTENTE, exception.getErro());
    }

    @Test
    void processarTransacao_SenhaInvalida_DeveLancarExcecao() {
        TransacaoDto transacaoComSenhaInvalida = TransacaoDto.builder()
                .numeroCartao("1234567890123456")
                .senhaCartao("4321") // senha invalida
                .valor(new BigDecimal("100.00"))
                .build();

        when(cartaoRepository.findByNumeroCartao(transacaoComSenhaInvalida.getNumeroCartao())).thenReturn(cartao);
        
        doThrow(new TransacaoNaoAutorizadaException("Senha inválida", AutorizacaoErro.SENHA_INVALIDA))
            .when(authorizationChain).authorize(transacaoComSenhaInvalida, cartao);

        TransacaoNaoAutorizadaException exception = assertThrows(
                TransacaoNaoAutorizadaException.class,
                () -> transacaoService.processarTransacao(transacaoComSenhaInvalida)
        );

        verify(cartaoRepository).findByNumeroCartao(transacaoComSenhaInvalida.getNumeroCartao());
        verify(authorizationChain).authorize(transacaoComSenhaInvalida, cartao);
        verify(cartaoRepository, never()).save(any(Cartao.class));
        
        assertEquals("Senha inválida", exception.getMessage());
        assertEquals(AutorizacaoErro.SENHA_INVALIDA, exception.getErro());
    }

    @Test
    void processarTransacao_SaldoInsuficiente_DeveLancarExcecao() {
        TransacaoDto transacaoComValorAlto = TransacaoDto.builder()
                .numeroCartao("1234567890123456")
                .senhaCartao("1234")
                .valor(new BigDecimal("600.00")) // valor maior que o saldo
                .build();

        when(cartaoRepository.findByNumeroCartao(transacaoComValorAlto.getNumeroCartao())).thenReturn(cartao);
        
        doThrow(new TransacaoNaoAutorizadaException("Saldo insuficiente", AutorizacaoErro.SALDO_INSUFICIENTE))
            .when(authorizationChain).authorize(transacaoComValorAlto, cartao);

        TransacaoNaoAutorizadaException exception = assertThrows(
                TransacaoNaoAutorizadaException.class,
                () -> transacaoService.processarTransacao(transacaoComValorAlto)
        );

        verify(cartaoRepository).findByNumeroCartao(transacaoComValorAlto.getNumeroCartao());
        verify(authorizationChain).authorize(transacaoComValorAlto, cartao);
        verify(cartaoRepository, never()).save(any(Cartao.class));
        
        assertEquals("Saldo insuficiente", exception.getMessage());
        assertEquals(AutorizacaoErro.SALDO_INSUFICIENTE, exception.getErro());
    }
}