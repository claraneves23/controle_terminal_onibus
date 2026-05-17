package com.controle.terminal.exception;

public record ApiFieldError(String field, Object rejectedValue, String message) {
}
