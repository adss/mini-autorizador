package com.example.miniautorizador.controller;

import com.example.miniautorizador.dto.CartaoDto;
import com.example.miniautorizador.service.CartaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/cartoes")
public class CartaoController {

    private final CartaoService cartaoService;

    @Autowired
    public CartaoController(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @PostMapping
    public ResponseEntity<CartaoDto> criarCartao(@Valid @RequestBody CartaoDto cartaoDto) {
        CartaoDto cartaoCriado = cartaoService.criarCartao(cartaoDto);
        return new ResponseEntity<>(cartaoCriado, HttpStatus.CREATED);
    }

    @GetMapping("/{numeroCartao}")
    public ResponseEntity<BigDecimal> obterSaldo(@PathVariable String numeroCartao) {
        BigDecimal saldo = cartaoService.obterSaldo(numeroCartao);
        return new ResponseEntity<>(saldo, HttpStatus.OK);
    }
}
