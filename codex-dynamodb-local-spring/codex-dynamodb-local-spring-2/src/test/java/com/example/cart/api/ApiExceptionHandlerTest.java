package com.example.cart.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.example.cart.service.DuplicateProductException;

class ApiExceptionHandlerTest {

	private final ApiExceptionHandler handler = new ApiExceptionHandler();

	@Test
	void mapsDuplicateProduct() {
		DuplicateProductException exception = new DuplicateProductException("user-1", "product-1",
				new RuntimeException());

		var problem = handler.duplicateProduct(exception);

		assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
		assertThat(problem.getTitle()).isEqualTo("Duplicate product");
		assertThat(problem.getProperties()).containsKey("timestamp").doesNotContainKey("errors");
	}

	@Test
	void mapsBodyValidationErrors() {
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getFieldErrors())
			.thenReturn(List.of(new FieldError("request", "unitPrice", "must be greater than 0")));
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		when(exception.getBindingResult()).thenReturn(bindingResult);

		var problem = handler.invalidBody(exception);

		assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(problem.getProperties().get("errors")).isEqualTo(List.of("unitPrice must be greater than 0"));
	}

	@Test
	void mapsPathValidationErrors() {
		@SuppressWarnings("unchecked")
		ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
		when(violation.getMessage()).thenReturn("must not be blank");
		ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

		var problem = handler.invalidPath(exception);

		assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(problem.getProperties().get("errors")).isEqualTo(List.of("must not be blank"));
	}

}
