package com.example.miniautorizador.exception;

import com.example.miniautorizador.enums.AutorizacaoErro;
import lombok.Getter;

@Getter
public class TransacaoNaoAutorizadaException extends RuntimeException {
    private final AutorizacaoErro erro;

    public TransacaoNaoAutorizadaException(String message, AutorizacaoErro erro) {
        super(message);
        this.erro = erro;
    }

}