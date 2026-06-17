package com.example.cart.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.cart.persistence.DuplicateProductException;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(DuplicateProductException.class)
	ProblemDetail duplicateProduct(DuplicateProductException exception) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
		problem.setTitle("Duplicate product");
		return problem;
	}

}
