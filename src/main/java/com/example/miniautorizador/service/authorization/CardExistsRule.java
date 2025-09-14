package com.example.miniautorizador.service.authorization;

import com.example.miniautorizador.dto.TransacaoDto;
import com.example.miniautorizador.enums.AutorizacaoErro;
import com.example.miniautorizador.exception.TransacaoNaoAutorizadaException;
import com.example.miniautorizador.model.Cartao;

/**
 * Regra que verifica se o cartão existe.
 */
public class CardExistsRule extends BaseAuthorizationRule {

    @Override
    public void authorize(TransacaoDto transacaoDto, Cartao cartao) {
        if (cartao == null) {
            throw new TransacaoNaoAutorizadaException("Cartão inexistente", AutorizacaoErro.CARTAO_INEXISTENTE);
        }

        checkNext(transacaoDto, cartao);
    }
}
