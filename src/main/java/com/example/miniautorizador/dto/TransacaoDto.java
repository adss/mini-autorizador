package com.example.miniautorizador.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoDto {
    @NotBlank(message = "Número do cartão é obrigatório")
    @Pattern(regexp = "^\\d{13,19}$", message = "Número do cartão deve conter entre 13 e 19 dígitos")
    private String numeroCartao;

    @NotBlank(message = "Senha do cartão é obrigatória")
    @Pattern(regexp = "^\\d{4,6}$", message = "Senha deve conter entre 4 e 6 dígitos")
    private String senhaCartao;

    @NotNull(message = "Valor da transação é obrigatório")
    @DecimalMin(value = "0.01", inclusive = true, message = "Valor deve ser maior que zero")
    private BigDecimal valor;
}
