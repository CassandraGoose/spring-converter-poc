package com.devetry.converter;

import java.util.List;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.FileInputStream;

import org.springframework.web.multipart.MultipartFile;

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
	private String extension;
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
		String serverFile = dir.getAbsolutePath() + File.separator;
		ImageInfo currentInfo = new ImageInfo(serverFile + "new.pdf");
		MagickImage currentImage = new MagickImage(currentInfo, bytes);
		try {
			currentImage.setFileName(serverFile + name + "-" +  date + ".tif");
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
}
