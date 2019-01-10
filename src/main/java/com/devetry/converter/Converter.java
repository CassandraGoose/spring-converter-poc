package com.devetry.converter;

import java.util.List;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.FileInputStream;

import java.io.FileWriter;

import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.io.FilenameUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

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
	private List<MultipartFile> uploadFiles;
	private String fileText;
	private File outputFile;
	private String path = System.getProperty("temp",File.separator+"tmp");
	String fullPath = path + File.separator + "tmpFiles";
	String serverFile;
	String serverFilePath;

	public Converter(String date, String applicant) {
		this.date = date;
		this.applicant = applicant;
	}

	public void convert() throws OfficeException, IOException, MagickException {
		System.out.println("convert");
		for (MultipartFile file : uploadFiles) {
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

	public void convert(File file) throws OfficeException, IOException, MagickException {
		name = file.getName();
		extension = FilenameUtils.getExtension(name);
		outputFile = new File(fullPath + name + ".pdf");
		final LocalOfficeManager officeManager = LocalOfficeManager.install();
		try {
			officeManager.start();
			FileInputStream targetStream = new FileInputStream(file);
			JodConverter.convert(targetStream)
					.as(DefaultDocumentFormatRegistry.getFormatByExtension(FilenameUtils.getExtension(name))).to(outputFile)
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

	public void prepForTiffConversion(File outputFile) throws IOException, MagickException {
		System.out.println("prepfortiffconversion");
		File dir = new File(fullPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try {
			FileInputStream stream = new FileInputStream(outputFile);
			byte[] bytes = Files.readAllBytes(outputFile.toPath());
			try {
				convertUploadedFiles(bytes);
				// sendToS3();
			} catch (MagickException e) {
				System.out.println(e.getMessage());
			}
			stream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void convertUploadedFiles(byte[] bytes) throws MagickException, IOException {
		System.out.println("convertuploadedfiles");
		System.setProperty("jmagick.systemclassloader", "no");
		File dir = new File(fullPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		serverFile = dir.getAbsolutePath() + File.separator;
		ImageInfo currentInfo = new ImageInfo(serverFile + "new.pdf");
		MagickImage currentImage = new MagickImage(currentInfo, bytes);
		try {
			serverFilePath = serverFile + name + "-" + applicant + "-" +  date + ".tif";
			currentImage.setFileName(serverFilePath);
		} catch (MagickException e) {
			System.out.println(e.getMessage());
		}
		currentImage.writeImage(currentInfo);
	}

	public void saveOriginalFile(List<MultipartFile> uploadedFiles) throws IOException {
		uploadFiles = uploadedFiles;
		for (MultipartFile file : uploadedFiles) {
			System.out.println("saveoriginalfile");
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

	public File saveTextFile(String text) throws IOException {
		fileText = text;
		File dir = new File(fullPath);
		name = applicant + "-text.txt";
		if (!dir.exists()) {
			dir.mkdirs();
		}
		Path finalPath = Paths.get(fullPath + File.separator + name);
		try {
			File file = new File(finalPath.toString());
			FileWriter writer = new FileWriter(file);
			writer.write(text);
			writer.flush();
			writer.close();
			byte[] bytes = Files.readAllBytes(file.toPath());
			Files.write(finalPath, bytes);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return new File(finalPath.toString());
	}

	public String sendToS3() throws IOException, AmazonServiceException, SdkClientException {
		String clientRegion = "us-west-2";
		String bucketName = "test-file-spring";
		String filePath = name + "-" + applicant + "-" + date + ".tif";

		File file = new File(serverFilePath);

		AWSCredentials credentials = new BasicAWSCredentials("AKIAIJ3K5SWJVIYHDQQA", "6Rhaf7lPniJVZ5d1K/g0yfhwf57VQkCC9HYYC9FS");

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
