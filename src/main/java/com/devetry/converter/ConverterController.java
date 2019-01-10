package com.devetry.converter;

import java.util.Arrays;
import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
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
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		String formattedDate = dateFormat.format(date);
		Converter textConverter = new Converter(formattedDate, "TestApplicant");
		System.out.println("Request Received.");
		try {
			System.out.println("Uploading Text for processing.");
			textFile = textConverter.saveTextFile(text);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			textConverter.convert(textFile);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity("Success?", HttpStatus.OK);
	}

	@RequestMapping(value = "/convert", method = RequestMethod.POST)
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile[] uploadedFiles) throws IOException, Exception {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		String formattedDate = dateFormat.format(date);
		Converter newConverter = new Converter(formattedDate, "TestApplicant");
		System.out.println("Request Received.");
		try {
			System.out.println("Uploading files for processing.");
			System.out.println(Arrays.asList(uploadedFiles).isEmpty());
			newConverter.saveOriginalFile(Arrays.asList(uploadedFiles));
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			System.out.println("Converting uploaded files.");
			newConverter.convert();
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity("Success?", HttpStatus.OK);
	}
}