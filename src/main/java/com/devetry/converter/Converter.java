package com.devetry.converter;

import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileWriter;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import org.apache.commons.io.FilenameUtils;

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
	private List<MultipartFile> uploadFiles;
	private File outputFile;
	private String path = System.getProperty("temp", File.separator + "tmp");
	String fullPath = path + File.separator + "tmpFiles";
	String serverFile;
	String serverFilePath;

	public Converter(String applicant) {
		this.applicant = applicant;
	}

	public void setUploadFiles(List<MultipartFile> uploadedFiles) throws IOException {
		uploadFiles = uploadedFiles;
	}

	public String setDate() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		String formattedDate = dateFormat.format(date);
		return formattedDate;
	}

	public MultipartFile createTextFile(String text) throws IOException {
		name = applicant + "-text.txt";
		File file = new File(fullPath + File.separator + name);
		FileWriter writer = new FileWriter(file);
		writer.write(text);
		writer.flush();
		writer.close();
		byte[] bytes = Files.readAllBytes(file.toPath());
		MultipartFile textMultipart = new MockMultipartFile(file.getName(), file.getName(), "text/plain", bytes);
		return textMultipart;
	}

	public void saveOriginalFile(List<MultipartFile> uploadedFiles) throws IOException {
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

	public String convertFile() throws OfficeException, IOException, MagickException {
		date = setDate();
		String finalPath = "";
		for (MultipartFile file : uploadFiles) {
			name = file.getOriginalFilename();
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
					finalPath = prepForTiffConversion(outputFile);
				} catch (MagickException e) {
					System.out.println(e.getMessage());
				}
				OfficeUtils.stopQuietly(officeManager);
			}
		}
		return finalPath;
	}

	public String prepForTiffConversion(File outputFile) throws IOException, MagickException {
		File dir = new File(fullPath);
		String serverFilePath = "";
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try {
			FileInputStream stream = new FileInputStream(outputFile);
			byte[] bytes = Files.readAllBytes(outputFile.toPath());
			try {
				serverFilePath = convertUploadedFiles(bytes);
			} catch (MagickException e) {
				System.out.println(e.getMessage());
			}
			stream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return serverFilePath;
	}

	public String convertUploadedFiles(byte[] bytes) throws MagickException, IOException {
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
		return serverFilePath;
	}
}
