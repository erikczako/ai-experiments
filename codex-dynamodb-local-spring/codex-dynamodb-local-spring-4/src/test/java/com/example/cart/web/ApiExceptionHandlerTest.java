package com.example.cart.web;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import com.example.cart.repository.DuplicateProductException;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

	@Test
	void mapsValidationErrors() throws Exception {
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
				new AddCartItemRequest("", BigDecimal.ONE), "request");
		bindingResult.addError(new FieldError("request", "productId", "must not be blank"));

		ProblemDetail problemDetail = new ApiExceptionHandler()
			.handleMethodArgumentNotValid(new MethodArgumentNotValidException(sampleParameter(), bindingResult));

		assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(problemDetail.getProperties()).containsEntry("errors", List.of("productId must not be blank"));
	}

	@Test
	void mapsBadRequest() {
		ProblemDetail problemDetail = new ApiExceptionHandler()
			.handleBadRequest(new IllegalArgumentException("bad request"));

		assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(problemDetail.getDetail()).isEqualTo("bad request");
	}

	@Test
	void mapsDuplicateProduct() {
		ProblemDetail problemDetail = new ApiExceptionHandler()
			.handleDuplicateProduct(new DuplicateProductException("sku-1"));

		assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
		assertThat(problemDetail.getDetail()).contains("already in the shopping cart");
	}

	private MethodParameter sampleParameter() throws NoSuchMethodException {
		Method method = ApiExceptionHandlerTest.class.getDeclaredMethod("sample", AddCartItemRequest.class);
		return new MethodParameter(method, 0);
	}

	@SuppressWarnings("unused")
	private void sample(AddCartItemRequest request) {
	}

}
