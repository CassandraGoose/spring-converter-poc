package com.devetry.converter;

import java.util.List;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.FileInputStream;

import org.springframework.web.multipart.MultipartFile;

import org.jodconverter.JodConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeUtils;
import org.jodconverter.office.LocalOfficeManager;

import magick.*;

public class Converter {
	private String date;
	private Boolean successful;
	private List<MultipartFile> uploadedFiles;
	private File outputFile;
	private String path = System.getProperty("temp",File.separator+"tmp");
	String fullPath = path + File.separator + "tmpFiles";

	public Converter(String date, List<MultipartFile> uploadedFiles) {
		this.date = date;
		this.uploadedFiles = uploadedFiles;
	}

	public void convert() throws OfficeException, IOException, MagickException {
		for (MultipartFile file : uploadedFiles) {
			outputFile = new File("new.pdf");
			final LocalOfficeManager officeManager = LocalOfficeManager.install();
			try {
				officeManager.start();
				JodConverter
					.convert(file.getInputStream())
					.as(DefaultDocumentFormatRegistry.DOC)
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
			} catch (MagickException e) {
				System.out.println(e.getMessage());
			}
			stream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("uploaded new.pdf (probably)!");
	}

	public void convertUploadedFiles(byte[] bytes) throws MagickException, IOException {
		System.setProperty("jmagick.systemclassloader", "no");
		File dir = new File(fullPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String serverFile = dir.getAbsolutePath() + File.separator;
		ImageInfo currentInfo = new ImageInfo(serverFile + "new.pdf");
		MagickImage currentImage = new MagickImage(currentInfo, bytes);
		Path finalPath = Paths.get(fullPath + File.separator + "new.tiff");

		try {
			currentImage.setFileName(serverFile + "new.tif");
		} catch (MagickException e) {
			System.out.println(e.getMessage());
		}
		currentImage.writeImage(currentInfo);
		byte[] newBytes = currentImage.imageToBlob(currentInfo);
		Files.write(finalPath, newBytes);
	}

	 public void saveUploadedFile() throws IOException {
		for (MultipartFile file : uploadedFiles) {
			if (file.isEmpty()) {
				continue;
			}
			File dir = new File(fullPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			byte[] bytes = file.getBytes();
			Path finalPath = Paths.get(fullPath + file.getOriginalFilename());

			Files.write(finalPath, bytes);
			System.out.println("uploaded " + file.getOriginalFilename() + "(probably)!");
		}
	}
	}
