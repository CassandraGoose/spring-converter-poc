package com.devetry.converter;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class ConvertedNotFoundAdvice {
	
	@ResponseBody
	@ExceptionHandler(ConvertedNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String convertedNotFoundHandler(ConvertedNotFoundException ex) {
		return ex.getMessage();
	}
}