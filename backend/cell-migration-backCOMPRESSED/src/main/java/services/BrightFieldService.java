package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import reactor.core.publisher.FluxSink;

@Service
public class BrightFieldService {

	@Value("${output.directory}")
	private String basePath;
	@Value("${generate.levelsets}")
	private String script_path;
	

	public void generateLevelsets(String no_iter, Long ID, FluxSink<String> sink) {
	    File resultDir = new File(this.basePath, "result" + ID);

	    ProcessBuilder processBuilder = new ProcessBuilder("python3", this.script_path, resultDir.getAbsolutePath(), no_iter);
	    processBuilder.redirectErrorStream(true); 

	    try {
	        Process process = processBuilder.start();

	        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                sink.next(line); 
	            }
	        }
	        int exitCode = process.waitFor();
	        sink.next("Process exited with code: " + exitCode);
	        sink.next("END");
	    } catch (IOException | InterruptedException e) {
	        sink.next("Error: " + e.getMessage());
	        Thread.currentThread().interrupt(); 
	    } finally {
	        sink.complete(); 
	    }
	}
	
	public void saveBrightFields(String resultName, List<MultipartFile> files) throws IOException {
	
		    File resultDir = new File(this.basePath, resultName);
		    if (!resultDir.exists()) {
		        throw new IOException("Directory doesnt exist: " + resultDir.getAbsolutePath());
		        
		    }
		    
		    File brightFieldDir = new File(resultDir, "BrightField");
		    if (!brightFieldDir.exists()) {
		        throw new IOException("Directory doesnt exist" + brightFieldDir.getAbsolutePath());
		        
		    }
	
		    for (MultipartFile f : files) {
		        String name = f.getOriginalFilename();
		        if (name == null || name.isEmpty()) {
		            throw new IOException("File name is invalid.");
		        }
		        File targetFile = new File(brightFieldDir, name);
		        f.transferTo(targetFile);
		
		        System.out.println("File saved: " + targetFile.getAbsolutePath());
		    }
	}

	
	
	
	
}
