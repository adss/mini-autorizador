package com.example.miniautorizador.repository;

import com.example.miniautorizador.model.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para operações de persistência da entidade Cartao.
 */
@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long> {
    
    /**
     * Busca um cartão pelo seu número.
     * 
     * @param numeroCartao o número do cartão a ser buscado
     * @return o cartão encontrado ou null se não existir
     */
    Cartao findByNumeroCartao(String numeroCartao);
}