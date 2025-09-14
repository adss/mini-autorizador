package com.example.miniautorizador.config;

import com.example.miniautorizador.domain.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessConfig {

    @Value("${cartao.saldo.inicial}")
    private String saldoInicialStr;

    @Bean
    public Money saldoInicial() {
        return Money.of(saldoInicialStr);
    }
}
