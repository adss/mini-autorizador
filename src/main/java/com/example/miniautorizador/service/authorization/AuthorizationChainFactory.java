package com.example.miniautorizador.service.authorization;

import org.springframework.stereotype.Component;

/**
 * Factory para criar a cadeia de regras de autorização.
 */
@Component
public class AuthorizationChainFactory {

    /**
     * Cria a cadeia de regras de autorização padrão.
     * A ordem das regras é importante:
     * 1. Verificar se o cartão existe
     * 2. Verificar se a senha é válida
     * 3. Verificar se o saldo é suficiente
     * 
     * @return A primeira regra da cadeia
     */
    public AuthorizationRule createDefaultChain() {
        AuthorizationRule cardExistsRule = new CardExistsRule();
        AuthorizationRule passwordValidRule = new PasswordValidRule();
        AuthorizationRule sufficientBalanceRule = new SufficientBalanceRule();

        cardExistsRule.setNext(passwordValidRule);
        passwordValidRule.setNext(sufficientBalanceRule);

        return cardExistsRule;
    }
}
