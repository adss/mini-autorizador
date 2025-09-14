package com.example.miniautorizador.controller;

import com.example.miniautorizador.dto.TransacaoDto;
import com.example.miniautorizador.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    @Autowired
    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public ResponseEntity<String> realizarTransacao(@Valid @RequestBody TransacaoDto transacaoDto) {
        transacaoService.processarTransacao(transacaoDto);
        return new ResponseEntity<>("OK", HttpStatus.CREATED);
    }
}
