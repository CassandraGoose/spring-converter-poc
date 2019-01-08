package com.devetry.converter;

class ConvertedNotFoundException extends RuntimeException {
	
	ConvertedNotFoundException(Long id) {
		super ("Could not find converted item " + id + ".");
	}
}