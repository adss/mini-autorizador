package com.example.miniautorizador.model;

import com.example.miniautorizador.domain.CardNumber;
import com.example.miniautorizador.domain.Money;
import com.example.miniautorizador.domain.Pin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "cartoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroCartao;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal saldo;

    @Version
    private Long version;

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo != null ? saldo.setScale(2, RoundingMode.HALF_EVEN) : null;
    }

    public CardNumber getNumeroCartaoVO() {
        return CardNumber.of(this.numeroCartao);
    }

    public void setNumeroCartaoFromVO(CardNumber cardNumber) {
        this.numeroCartao = cardNumber.getValue();
    }

    public Pin getSenhaVO() {
        return Pin.fromHashed(this.senha);
    }

    public void setSenhaFromVO(Pin pin) {
        this.senha = pin.getHashedValue();
    }

    public Money getSaldoVO() {
        return Money.of(this.saldo);
    }

    public void setSaldoFromVO(Money money) {
        this.saldo = money.getAmount();
    }

    public boolean verificarSenha(String senhaFornecida) {
        return getSenhaVO().matches(senhaFornecida);
    }

    public Money debitarSaldo(Money valor) {
        Money saldoAtual = getSaldoVO();
        if (valor.isGreaterThan(saldoAtual)) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        Money novoSaldo = saldoAtual.subtract(valor);
        setSaldoFromVO(novoSaldo);
        return novoSaldo;
    }
}
