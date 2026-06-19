package com.example.cart.web;

import java.net.URI;

import com.example.cart.repository.DuplicateProductException;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Request validation failed.");
		problemDetail.setType(URI.create("https://example.com/problems/validation"));
		problemDetail.setProperty("errors",
				ex.getBindingResult()
					.getFieldErrors()
					.stream()
					.map((error) -> error.getField() + " " + error.getDefaultMessage())
					.toList());
		return problemDetail;
	}

	@ExceptionHandler({ ConstraintViolationException.class, IllegalArgumentException.class,
			MissingRequestHeaderException.class })
	ProblemDetail handleBadRequest(Exception ex) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problemDetail.setType(URI.create("https://example.com/problems/bad-request"));
		return problemDetail;
	}

	@ExceptionHandler(DuplicateProductException.class)
	ProblemDetail handleDuplicateProduct(DuplicateProductException ex) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problemDetail.setType(URI.create("https://example.com/problems/duplicate-product"));
		return problemDetail;
	}

}
