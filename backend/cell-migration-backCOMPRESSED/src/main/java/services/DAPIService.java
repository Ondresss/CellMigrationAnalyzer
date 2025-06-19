package services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DAPIService {

	@Value("${output.directory}")
	private String basePath;
	
	public void saveDapiFiles(List<MultipartFile> files, Long id) throws IOException {
	    File resultDir = new File(this.basePath, "result" + id + "/DAPI");

	    if (!resultDir.exists() || !resultDir.isDirectory()) {
	        throw new IOException("Result directory does not exist: " + resultDir.getAbsolutePath());
	    }

	    for (MultipartFile file : files) {
	        String originalFilename = file.getOriginalFilename();
	        if (originalFilename == null || originalFilename.isEmpty()) {
	            throw new IOException("Invalid file name.");
	        }
	        File dapiFile = new File(resultDir, originalFilename);
	        if (dapiFile.exists()) {
	            String newFilename = System.currentTimeMillis() + "_" + originalFilename;
	            dapiFile = new File(resultDir, newFilename);
	        }

	        file.transferTo(dapiFile);
	        System.out.println("Saved DAPI file: " + dapiFile.getAbsolutePath());
	    }
	}
	

}
