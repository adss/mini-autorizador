package com.example.miniautorizador.exception;

import com.example.miniautorizador.dto.CartaoDto;
import com.example.miniautorizador.enums.AutorizacaoErro;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        if (ex.getTarget() != null && ex.getTarget().getClass().getSimpleName().equals("TransacaoDto")) {
            for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                String field = error.getField();

                if (field.equals("senhaCartao")) {
                    return new ResponseEntity<>(AutorizacaoErro.SENHA_INVALIDA.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
                } else if (field.equals("numeroCartao")) {
                    return new ResponseEntity<>(AutorizacaoErro.CARTAO_INEXISTENTE.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
                } else if (field.equals("valor")) {
                    return new ResponseEntity<>(AutorizacaoErro.VALOR_INVALIDO.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }
        }

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CartaoJaExistenteException.class)
    public ResponseEntity<CartaoDto> handleCartaoJaExistenteException(
            CartaoJaExistenteException ex, WebRequest request) {

        CartaoDto cartaoDto = ex.getCartaoDto();

        return new ResponseEntity<>(cartaoDto, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(CartaoNaoEncontradoException.class)
    public ResponseEntity<Void> handleCartaoNaoEncontradoException(
            CartaoNaoEncontradoException ex, WebRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TransacaoNaoAutorizadaException.class)
    public ResponseEntity<String> handleTransacaoNaoAutorizadaException(
            TransacaoNaoAutorizadaException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getErro().toString(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Ocorreu um erro interno no servidor");
        error.put("details", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
