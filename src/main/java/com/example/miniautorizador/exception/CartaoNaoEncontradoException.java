package com.example.miniautorizador.exception;

public class CartaoNaoEncontradoException extends RuntimeException {
    public CartaoNaoEncontradoException(String message) {
        super(message);
    }
}