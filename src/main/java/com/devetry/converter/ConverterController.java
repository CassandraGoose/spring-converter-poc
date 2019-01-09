package com.devetry.converter;

import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
public class ConverterController {
	
	public ConverterController() {
		super();
	}
	
	@PostMapping("/convert")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadedFiles) 
		throws IOException, Exception {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyy");
		String formattedDate = dateFormat.format(date);
		Converter newConverter = new Converter(formattedDate, Arrays.asList(uploadedFiles));
		System.out.println("Request Received.");
		try {
			System.out.println("Uploading files for processing.");
			newConverter.saveUploadedFile();
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			System.out.println("Converting uploaded files.");
			newConverter.convert();
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		System.out.println("Something happened, that's for certain.");
		return new ResponseEntity<>(HttpStatus.OK);
	}
}