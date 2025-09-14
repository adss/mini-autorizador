package com.example.miniautorizador.service.authorization;

import com.example.miniautorizador.dto.TransacaoDto;
import com.example.miniautorizador.enums.AutorizacaoErro;
import com.example.miniautorizador.exception.TransacaoNaoAutorizadaException;
import com.example.miniautorizador.model.Cartao;

/**
 * Regra que verifica se a senha do cartão é válida.
 */
public class PasswordValidRule extends BaseAuthorizationRule {

    @Override
    public void authorize(TransacaoDto transacaoDto, Cartao cartao) {
        if (transacaoDto.getSenhaCartao() == null) {
            throw new TransacaoNaoAutorizadaException("Senha não pode ser nula", AutorizacaoErro.SENHA_INVALIDA);
        }

        if (!cartao.verificarSenha(transacaoDto.getSenhaCartao())) {
            throw new TransacaoNaoAutorizadaException("Senha inválida", AutorizacaoErro.SENHA_INVALIDA);
        }

        checkNext(transacaoDto, cartao);
    }
}
