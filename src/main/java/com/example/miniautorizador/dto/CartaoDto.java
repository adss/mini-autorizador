package com.example.miniautorizador.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartaoDto {
    @NotBlank(message = "Número do cartão é obrigatório")
    @Pattern(regexp = "^\\d{13,19}$", message = "Número do cartão deve conter entre 13 e 19 dígitos")
    private String numeroCartao;

    @NotBlank(message = "Senha é obrigatória")
    @Pattern(regexp = "^\\d{4,6}$", message = "Senha deve conter entre 4 e 6 dígitos")
    private String senha;
}
