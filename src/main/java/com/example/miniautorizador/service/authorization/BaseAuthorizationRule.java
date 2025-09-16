package com.example.miniautorizador.service.authorization;

import com.example.miniautorizador.dto.TransacaoDto;
import com.example.miniautorizador.model.Cartao;

/**
 * Implementação base para regras de autorização.
 * Fornece a estrutura básica para o padrão Chain of Responsibility.
 */
public abstract class BaseAuthorizationRule implements AuthorizationRule {
    
    private AuthorizationRule next;
    
    @Override
    public AuthorizationRule setNext(AuthorizationRule next) {
        this.next = next;
        return next;
    }
    
    /**
     * Executa a próxima regra na cadeia, se existir.
     * 
     * @param transacaoDto Os dados da transação
     * @param cartao O cartão associado à transação
     */
    protected void checkNext(TransacaoDto transacaoDto, Cartao cartao) {
        if (next != null) {
            next.authorize(transacaoDto, cartao);
        }
    }
}