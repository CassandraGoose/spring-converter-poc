package com.devetry.converter;

import java.util.List;
import java.util.Arrays;
import java.util.Map;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.jodconverter.DocumentConverter;
import org.jodconverter.JodConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeUtils;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.LocalOfficeManager;

import magick.*;

@RestController
public class ConverterController {
	private final ConverterRepository repository;
	
	public ConverterController(ConverterRepository repository) {
		super();
		this.repository = repository;
	}
	
	@GetMapping("/converter")
	List<Converter> all() {
		return repository.findAll();
	}
	
	@PostMapping("/convert")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadedFiles) throws IOException, Exception {
		// we'll only want to save like the date and status of the conversion
		// the rest of the body will be converted to tiff and sent off to their crm or s3 or whatever. 
		// return repository.save("converted?");

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
	
	@GetMapping("converted/{id}")
	Converter one(@PathVariable long id) {
		return repository.findById(id)
				.orElseThrow(() -> new ConvertedNotFoundException(id));
	}

	// private ResponseEntity<Object> convert(
	// 	final MultipartFile uploadedFiles,
	// 	final String outputFormat,
	// 	final Map<String, String> parameters) {
	// 		if (uploadedFiles.isEmpty()) {
	// 			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	// 		}
	// 		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	// 			final DocumentFormat targetFormat = DefaultDocumentFormatRegistry.PDF;
				
	// 			final DocumentConverter converter = LocalConverter.builder().officeManager(officeManager)
	// 				.loadProperties(loadProperties)
	// 				.storeProperties(storeProperties)
	// 				.build();
	// 		}
	// 	}

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
			// Path finalPath = Paths.get(fullPath + File.separator + "new.pdf");
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

				// currentImage.breakFrames();
				try { 
					currentImage.setFileName(serverFile + "new.tif");
				} catch (MagickException e) {
					System.out.println(e.getMessage());
				}
				currentImage.writeImage(currentInfo);
				byte[] newBytes = currentImage.imageToBlob(currentInfo);
				// BufferedOutputStream stream = new BufferedOutputStream(new
				// FileOutputStream(serverFile));
				Files.write(finalPath, newBytes);
				// stream.close();
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