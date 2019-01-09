package com.devetry.converter;

import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.FileInputStream;

import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.io.FilenameUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;

import org.jodconverter.JodConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeUtils;
import org.jodconverter.office.LocalOfficeManager;

import magick.*;

public class Converter {
	private String date;
	private String name;
	private String applicant;
	private String extension;
	private List<MultipartFile> uploadedFiles;
	private File outputFile;
	private String path = System.getProperty("temp",File.separator+"tmp");
	String fullPath = path + File.separator + "tmpFiles";
	String serverFile;

	public Converter(String date, String applicant, List<MultipartFile> uploadedFiles) {
		this.date = date;
		this.applicant = applicant;
		this.uploadedFiles = uploadedFiles;
	}

	public void convert() throws OfficeException, IOException, MagickException {
		for (MultipartFile file : uploadedFiles) {
			name = file.getOriginalFilename();
			extension = FilenameUtils.getExtension(name);
			outputFile = new File(fullPath + name + ".pdf");
			final LocalOfficeManager officeManager = LocalOfficeManager.install();
			try {
				officeManager.start();
				JodConverter
					.convert(file.getInputStream())
					.as(DefaultDocumentFormatRegistry.getFormatByExtension(FilenameUtils.getExtension(name)))
					.to(outputFile)
					.execute();
			} catch (OfficeException | IOException e) {
				System.out.println(e.getMessage());
			} finally {
				try {
					prepForTiffConversion(outputFile);
				} catch (MagickException e) {
					System.out.println(e.getMessage());
				}
				OfficeUtils.stopQuietly(officeManager);
			}
		}
	}

	public void prepForTiffConversion(File outputFile) throws IOException, MagickException {
		File dir = new File(fullPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try {
			FileInputStream stream = new FileInputStream(outputFile);
			byte[] bytes = Files.readAllBytes(outputFile.toPath());
			try {
				convertUploadedFiles(bytes);
				sendToS3();
			} catch (MagickException e) {
				System.out.println(e.getMessage());
			}
			stream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void convertUploadedFiles(byte[] bytes) throws MagickException, IOException {
		System.setProperty("jmagick.systemclassloader", "no");
		File dir = new File(fullPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		serverFile = dir.getAbsolutePath() + File.separator;
		ImageInfo currentInfo = new ImageInfo(serverFile + "new.pdf");
		MagickImage currentImage = new MagickImage(currentInfo, bytes);
		try {
			currentImage.setFileName(serverFile + name + "-" + applicant + "-" +  date + ".tif");
		} catch (MagickException e) {
			System.out.println(e.getMessage());
		}
		currentImage.writeImage(currentInfo);
	}

	public void saveOriginalFile() throws IOException {
		for (MultipartFile file : uploadedFiles) {
			try {
				name = file.getOriginalFilename();
				if (file.isEmpty()) {
					continue;
				}
				File dir = new File(fullPath);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				byte[] bytes = file.getBytes();
				Path finalPath = Paths.get(fullPath + File.separator + name);
				Files.write(finalPath, bytes);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public String sendToS3() throws IOException, AmazonServiceException, SdkClientException {
		String clientRegion = "us-west-2";
		String bucketName = "test-file-spring";
		String filePath = serverFile + name + "-" + applicant + "-" + date + ".tif";

		File file = new File(filePath);

		AWSCredentials credentials = new BasicAWSCredentials("KEYHERE", "SECRETHERE");

		AmazonS3 s3client = AmazonS3ClientBuilder
			.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials))
			.withRegion(clientRegion)
			.build();
	
		try {
			s3client.putObject(bucketName, filePath, file);
		} catch (AmazonServiceException e) {
			e.printStackTrace();
		} catch (SdkClientException e) {
			e.printStackTrace();
		}
		return "Attempted to upload to S3.";
	}
}
