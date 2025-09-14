package com.example.miniautorizador.service;

import com.example.miniautorizador.domain.Money;
import com.example.miniautorizador.dto.CartaoDto;
import com.example.miniautorizador.exception.CartaoJaExistenteException;
import com.example.miniautorizador.exception.CartaoNaoEncontradoException;
import com.example.miniautorizador.model.Cartao;
import com.example.miniautorizador.repository.CartaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;
    
    @Mock
    private Money saldoInicial;
    
    @Mock
    private Money cartaoSaldoVO;
    
    @Mock
    private Cartao cartao;
    
    private CartaoService cartaoService;

    private CartaoDto cartaoDto;

    @BeforeEach
    void setUp() {
        cartaoService = new CartaoService(cartaoRepository, saldoInicial);
        
        cartaoDto = CartaoDto.builder()
                .numeroCartao("1234567890123456")
                .senha("1234")
                .build();
    }

    @Test
    void criarCartao_ComNumeroInexistente_DeveCriarComSaldoInicial500() {
        when(saldoInicial.getAmount()).thenReturn(new BigDecimal("500.00"));
        when(cartaoRepository.findByNumeroCartao(cartaoDto.getNumeroCartao())).thenReturn(null);
        when(cartaoRepository.save(any(Cartao.class))).thenAnswer(invocation -> {
            Cartao savedCartao = invocation.getArgument(0);
            savedCartao.setId(1L);
            return savedCartao;
        });

        CartaoDto resultado = cartaoService.criarCartao(cartaoDto);

        verify(cartaoRepository).findByNumeroCartao(cartaoDto.getNumeroCartao());
        verify(cartaoRepository).save(any(Cartao.class));
        assertEquals(cartaoDto.getNumeroCartao(), resultado.getNumeroCartao());
        assertEquals(cartaoDto.getSenha(), resultado.getSenha());
    }

    @Test
    void criarCartao_ComNumeroJaExistente_DeveLancarExcecao() {
        when(cartaoRepository.findByNumeroCartao(cartaoDto.getNumeroCartao())).thenReturn(cartao);

        CartaoJaExistenteException exception = assertThrows(
                CartaoJaExistenteException.class,
                () -> cartaoService.criarCartao(cartaoDto)
        );

        verify(cartaoRepository).findByNumeroCartao(cartaoDto.getNumeroCartao());
        verify(cartaoRepository, never()).save(any(Cartao.class));
        assertEquals("Cartão já existente", exception.getMessage());
        assertEquals(cartaoDto, exception.getCartaoDto());
    }

    @Test
    void obterSaldo_CartaoExistente_DeveRetornarSaldoCorreto() {
        when(cartaoRepository.findByNumeroCartao(cartaoDto.getNumeroCartao())).thenReturn(cartao);
        when(cartao.getSaldoVO()).thenReturn(cartaoSaldoVO);
        when(cartaoSaldoVO.getAmount()).thenReturn(new BigDecimal("500.00"));

        BigDecimal saldo = cartaoService.obterSaldo(cartaoDto.getNumeroCartao());

        verify(cartaoRepository).findByNumeroCartao(cartaoDto.getNumeroCartao());
        assertEquals(new BigDecimal("500.00"), saldo);
        assertEquals(2, saldo.scale());
    }

    @Test
    void obterSaldo_CartaoInexistente_DeveLancarExcecao() {
        when(cartaoRepository.findByNumeroCartao(cartaoDto.getNumeroCartao())).thenReturn(null);

        CartaoNaoEncontradoException exception = assertThrows(
                CartaoNaoEncontradoException.class,
                () -> cartaoService.obterSaldo(cartaoDto.getNumeroCartao())
        );

        verify(cartaoRepository).findByNumeroCartao(cartaoDto.getNumeroCartao());
        assertEquals("Cartão não encontrado", exception.getMessage());
    }
}