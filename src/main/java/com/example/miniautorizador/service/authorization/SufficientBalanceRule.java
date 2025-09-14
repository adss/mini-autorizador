package com.example.miniautorizador.service.authorization;

import com.example.miniautorizador.domain.Money;
import com.example.miniautorizador.dto.TransacaoDto;
import com.example.miniautorizador.enums.AutorizacaoErro;
import com.example.miniautorizador.exception.TransacaoNaoAutorizadaException;
import com.example.miniautorizador.model.Cartao;

/**
 * Regra que verifica se o cartão possui saldo suficiente para a transação.
 */
public class SufficientBalanceRule extends BaseAuthorizationRule {

    @Override
    public void authorize(TransacaoDto transacaoDto, Cartao cartao) {
        if (transacaoDto.getValor() == null) {
            throw new TransacaoNaoAutorizadaException("Valor não pode ser nulo", AutorizacaoErro.VALOR_INVALIDO);
        }

        Money valorTransacao = Money.of(transacaoDto.getValor());

        if (valorTransacao.isZero() || valorTransacao.isNegative()) {
            throw new TransacaoNaoAutorizadaException("Valor deve ser maior que zero", AutorizacaoErro.VALOR_INVALIDO);
        }

        Money saldoCartao = cartao.getSaldoVO();

        if (valorTransacao.isGreaterThan(saldoCartao)) {
            throw new TransacaoNaoAutorizadaException("Saldo insuficiente", AutorizacaoErro.SALDO_INSUFICIENTE);
        }

        checkNext(transacaoDto, cartao);
    }
}
