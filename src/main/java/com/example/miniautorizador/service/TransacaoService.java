package com.example.miniautorizador.service;

import com.example.miniautorizador.domain.Money;
import com.example.miniautorizador.dto.TransacaoDto;
import com.example.miniautorizador.model.Cartao;
import com.example.miniautorizador.repository.CartaoRepository;
import com.example.miniautorizador.service.authorization.AuthorizationChainFactory;
import com.example.miniautorizador.service.authorization.AuthorizationRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável por processar transações de cartões.
 * Implementa o princípio de responsabilidade única (SRP).
 */
@Service
public class TransacaoService {

    private final CartaoRepository cartaoRepository;
    private final AuthorizationChainFactory authorizationChainFactory;

    @Autowired
    public TransacaoService(CartaoRepository cartaoRepository, 
                           AuthorizationChainFactory authorizationChainFactory) {
        this.cartaoRepository = cartaoRepository;
        this.authorizationChainFactory = authorizationChainFactory;
    }

    /**
     * Autoriza e processa uma transação.
     * 
     * @param transacaoDto Os dados da transação
     */
    @Transactional
    public void processarTransacao(TransacaoDto transacaoDto) {
        Cartao cartao = buscarCartao(transacaoDto.getNumeroCartao());
        autorizarTransacao(transacaoDto, cartao);
        debitarSaldo(cartao, transacaoDto);
        salvarCartao(cartao);
    }

    /**
     * Busca um cartão pelo número.
     */
    private Cartao buscarCartao(String numeroCartao) {
        return cartaoRepository.findByNumeroCartao(numeroCartao);
    }

    /**
     * Autoriza uma transação aplicando todas as regras de autorização.
     */
    private void autorizarTransacao(TransacaoDto transacaoDto, Cartao cartao) {
        AuthorizationRule authorizationChain = authorizationChainFactory.createDefaultChain();
        authorizationChain.authorize(transacaoDto, cartao);
    }

    /**
     * Debita o valor da transação do saldo do cartão.
     */
    private void debitarSaldo(Cartao cartao, TransacaoDto transacaoDto) {
        Money valorTransacao = Money.of(transacaoDto.getValor());
        cartao.debitarSaldo(valorTransacao);
    }

    /**
     * Salva as alterações no cartão.
     */
    private void salvarCartao(Cartao cartao) {
        cartaoRepository.save(cartao);
    }
}
