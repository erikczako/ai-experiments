package com.example.shoppingcart.api;

import java.time.Instant;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.shoppingcart.domain.DuplicateProductException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(DuplicateProductException.class)
	ProblemDetail handleDuplicateProduct(DuplicateProductException exception) {
		return problem(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class })
	ProblemDetail handleValidation(Exception exception) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Request validation failed");
		if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
			Map<String, String> errors = methodArgumentNotValidException.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(java.util.stream.Collectors.toMap(error -> error.getField(),
						error -> error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage(),
						(first, second) -> first));
			problem.setProperty("errors", errors);
		}
		return problem;
	}

	private static ProblemDetail problem(HttpStatus status, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

}
