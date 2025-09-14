package com.example.miniautorizador.service;

import com.example.miniautorizador.domain.CardNumber;
import com.example.miniautorizador.domain.Money;
import com.example.miniautorizador.domain.Pin;
import com.example.miniautorizador.dto.CartaoDto;
import com.example.miniautorizador.exception.CartaoJaExistenteException;
import com.example.miniautorizador.exception.CartaoNaoEncontradoException;
import com.example.miniautorizador.model.Cartao;
import com.example.miniautorizador.repository.CartaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CartaoService {

    private final CartaoRepository cartaoRepository;
    private final Money saldoInicial;

    @Autowired
    public CartaoService(CartaoRepository cartaoRepository, Money saldoInicial) {
        this.cartaoRepository = cartaoRepository;
        this.saldoInicial = saldoInicial;
    }

    @Transactional
    public CartaoDto criarCartao(CartaoDto cartaoDto) {
        if (cartaoDto.getNumeroCartao() == null || cartaoDto.getSenha() == null) {
            throw new IllegalArgumentException("Número do cartão e senha não podem ser nulos");
        }

        Cartao cartaoExistente = buscarCartaoPorNumero(cartaoDto.getNumeroCartao());
        if (cartaoExistente != null) {
            throw new CartaoJaExistenteException("Cartão já existente", cartaoDto);
        }

        Cartao novoCartao = criarNovoCartao(cartaoDto);
        salvarCartao(novoCartao);

        return cartaoDto;
    }

    @Transactional(readOnly = true)
    public BigDecimal obterSaldo(String numeroCartao) {
        if (numeroCartao == null) {
            throw new IllegalArgumentException("Número do cartão não pode ser nulo");
        }

        Cartao cartao = buscarCartaoPorNumero(numeroCartao);
        if (cartao == null) {
            throw new CartaoNaoEncontradoException("Cartão não encontrado");
        }

        return cartao.getSaldoVO().getAmount();
    }

    private Cartao buscarCartaoPorNumero(String numeroCartao) {
        return cartaoRepository.findByNumeroCartao(numeroCartao);
    }

    private Cartao criarNovoCartao(CartaoDto cartaoDto) {
        CardNumber cardNumber = CardNumber.of(cartaoDto.getNumeroCartao());
        Pin pin = Pin.of(cartaoDto.getSenha());

        Cartao novoCartao = Cartao.builder()
                .numeroCartao(cardNumber.getValue())
                .senha(pin.getHashedValue())
                .saldo(saldoInicial.getAmount())
                .build();

        return novoCartao;
    }

    /**
     * Salva um cartão no repositório.
     */
    private void salvarCartao(Cartao cartao) {
        cartaoRepository.save(cartao);
    }
}
