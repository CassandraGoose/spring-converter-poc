package com.devetry.converter;

import java.util.List;
import java.util.Arrays;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.FileInputStream;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.jodconverter.JodConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeUtils;
import org.jodconverter.office.LocalOfficeManager;

import magick.*;

@RestController
public class ConverterController {
	
	public ConverterController() {
		super();
	}
	
	@PostMapping("/convert")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadedFiles) throws IOException, Exception {

		try {
			this.saveUploadedFile(Arrays.asList(uploadedFiles));
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			convert(Arrays.asList(uploadedFiles));
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity("Successful upload probably", HttpStatus.OK);
	}

	private ResponseEntity<Object> convert(final List<MultipartFile> uploadedFiles) throws OfficeException, IOException, MagickException {
		for (MultipartFile file : uploadedFiles) {
			File outputFile = new File("new.pdf");
			final LocalOfficeManager officeManager = LocalOfficeManager.install();
			try {
				officeManager.start();

				JodConverter.convert(file.getInputStream()).as(DefaultDocumentFormatRegistry.DOC).to(outputFile).execute();
			} catch (OfficeException | IOException e) {
				System.out.println(e.getMessage());
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			} finally {
				try {
					this.prepForTiffConversion(outputFile);
				} catch (MagickException e) {
					System.out.println("holy moley");
				}
				OfficeUtils.stopQuietly(officeManager);
			}
		}	
		return new ResponseEntity("Maybe?", HttpStatus.OK);
	}

	private void prepForTiffConversion(File outputFile) throws IOException, MagickException {
		String path = System.getProperty("temp", File.separator + "tmp");
		String fullPath = path + File.separator + "tmpFiles" + File.separator;
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

	public void convertUploadedFiles(byte[] bytes)
	throws MagickException, IOException {
				System.setProperty("jmagick.systemclassloader", "no");
				String path = System.getProperty("temp", File.separator + "tmp");
				String fullPath = path + File.separator + "tmpFiles";
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

	private void saveUploadedFile(List<MultipartFile> uploadedFiles) throws IOException {
		for (MultipartFile file : uploadedFiles) {
			if (file.isEmpty()) {
				continue;
			}
			String path = System.getProperty("temp", File.separator + "tmp");
			String fullPath = path + File.separator + "tmpFiles" + File.separator;
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