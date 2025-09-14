package com.example.miniautorizador.service.authorization;

import com.example.miniautorizador.dto.TransacaoDto;
import com.example.miniautorizador.model.Cartao;

/**
 * Interface para regras de autorização de transações.
 * Implementa o padrão Chain of Responsibility.
 */
public interface AuthorizationRule {
    
    /**
     * Verifica se a transação atende a esta regra de autorização.
     * 
     * @param transacaoDto Os dados da transação
     * @param cartao O cartão associado à transação (pode ser null se o cartão não existir)
     * @throws com.example.miniautorizador.exception.TransacaoNaoAutorizadaException Se a regra não for atendida
     */
    void authorize(TransacaoDto transacaoDto, Cartao cartao);
    
    /**
     * Define a próxima regra na cadeia.
     * 
     * @param next A próxima regra a ser verificada
     * @return A próxima regra
     */
    AuthorizationRule setNext(AuthorizationRule next);
    
    /**
     * Obtém a próxima regra na cadeia.
     * 
     * @return A próxima regra ou null se for a última
     */
    AuthorizationRule getNext();
}