package com.example.cart.api;

import java.time.Instant;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.cart.service.DuplicateProductException;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(DuplicateProductException.class)
	ProblemDetail duplicateProduct(DuplicateProductException exception) {
		return problem(HttpStatus.CONFLICT, "Duplicate product", exception.getMessage(), List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail invalidBody(MethodArgumentNotValidException exception) {
		List<String> errors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> "%s %s".formatted(error.getField(), error.getDefaultMessage()))
			.toList();
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", "Request body validation failed", errors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ProblemDetail invalidPath(ConstraintViolationException exception) {
		List<String> errors = exception.getConstraintViolations()
			.stream()
			.map(violation -> violation.getMessage())
			.toList();
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", "Path validation failed", errors);
	}

	private ProblemDetail problem(HttpStatus status, String title, String detail, List<String> errors) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		problem.setProperty("timestamp", Instant.now());
		if (!errors.isEmpty()) {
			problem.setProperty("errors", errors);
		}
		return problem;
	}

}
