package com.devetry.converter;

import java.util.Arrays;
import java.io.File;
import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
public class ConverterController {
	public File textFile;
	public ConverterController() {
		super();
	}
	
	@RequestMapping(value = "/convert", method = RequestMethod.POST, params = "text")
	public ResponseEntity<?> uploadText(String text) throws IOException, Exception {
		Converter textConverter = new Converter("TestApplicant");
		try {
			textFile = textConverter.saveTextFile(text);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			textConverter.convert(textFile);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity("Success?👹", HttpStatus.OK);
	}

	@RequestMapping(value = "/convert", method = RequestMethod.POST)
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile[] uploadedFiles) throws IOException, Exception {
		Converter newConverter = new Converter("TestApplicant");
		try {
			System.out.println(Arrays.asList(uploadedFiles).isEmpty());
			newConverter.saveOriginalFile(Arrays.asList(uploadedFiles));
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			newConverter.convert();
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity("Success?👹", HttpStatus.OK);
	}
}