package com.example.miniautorizador.exception;

import com.example.miniautorizador.dto.CartaoDto;

public class CartaoJaExistenteException extends RuntimeException {
    private final CartaoDto cartaoDto;

    public CartaoJaExistenteException(String message, CartaoDto cartaoDto) {
        super(message);
        this.cartaoDto = cartaoDto;
    }

    public CartaoDto getCartaoDto() {
        return cartaoDto;
    }
}
