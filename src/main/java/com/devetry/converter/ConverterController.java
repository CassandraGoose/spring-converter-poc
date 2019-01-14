package com.devetry.converter;

import com.devetry.converter.S3Controller;

import java.util.Arrays;
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
	public MultipartFile textfile;
	public String applicant = "TestApplicant";
	private String clientRegion = "us-west-2";
	private String bucketName = "test-file-spring";
	private String key = "";
	private String secret = "";
	String res = "hey";

	public ConverterController() {
		super();
	}
	
	@RequestMapping(value = "/convert", method = RequestMethod.POST, params = "text")
	public ResponseEntity<?> uploadText(String text) throws IOException, Exception {
		Converter textConverter = new Converter(applicant);
		try {
			textfile = textConverter.createTextFile(text);
			textConverter.setUploadFiles(Arrays.asList(textfile));
			textConverter.saveOriginalFile(Arrays.asList(textfile));
		} catch (IOException e) {
			return new ResponseEntity<String>(res, HttpStatus.BAD_REQUEST);
		}
		
		String filePath = textConverter.convertFile();
		System.out.println(filePath);

		S3Controller sender = new S3Controller(filePath, applicant, clientRegion, bucketName, key, secret);
		sender.sendToS3();
			return new ResponseEntity<String>(res, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/convert", method = RequestMethod.POST)
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile[] uploadedFiles) throws IOException, Exception {
		Converter fileConverter = new Converter("TestApplicant");
		try {
			fileConverter.setUploadFiles(Arrays.asList(uploadedFiles));
			fileConverter.saveOriginalFile(Arrays.asList(uploadedFiles));
		} catch (IOException e) {
			return new ResponseEntity<String>(res, HttpStatus.BAD_REQUEST);
		}

		String filePath = fileConverter.convertFile();
		S3Controller sender = new S3Controller(filePath, applicant, clientRegion, bucketName, key, secret);
		sender.sendToS3();
			return new ResponseEntity<String>(res, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}