package com.devetry.converter;

import lombok.Data;
import magick.*;
import org.jodconverter.LocalConverter;

import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.apache.commons.io.FilenameUtils;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.web.multipart.MultipartFile;

@Data
@Entity
public class Converter {
	private @Id @GeneratedValue Long id;
	private String date;
	private Boolean successful;

	public Converter() {}
	public Converter(String date, Boolean successful) {
		this.date = date;
		this.successful = successful;
	}

	// public static void convertUploadedFiles(List<MultipartFile> uploadedFiles) throws MagickException, IOException {
	// 	System.setProperty("jmagick.systemclassloader", "no");
	// 	for (MultipartFile file : uploadedFiles) {
	// 		if (file.isEmpty()) {
	// 			continue;
	// 		}
	// 		String name = file.getOriginalFilename();
	// 		byte[] bytes = file.getBytes();
	// 		String path = System.getProperty("temp", File.separator + "tmp");
	// 		String fullPath = path + File.separator + "tmpFiles";
	// 		File dir = new File(fullPath);
	// 		if (!dir.exists()) {
	// 			dir.mkdirs();
	// 		} 
	// 		File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
	// 		System.out.println(bytes);
	// 		ImageInfo currentInfo = new ImageInfo(name);
	// 		MagickImage currentImage = new MagickImage(currentInfo, bytes);
	// 		currentImage.setFileName("new.tiff");
	// 		currentImage.writeImage(currentInfo);
	// 		byte[] newBytes = currentImage.imageToBlob(currentInfo);
	// 		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
	// 		stream.write(newBytes);
	// 		stream.close();
	// 	}
	// }

	}
