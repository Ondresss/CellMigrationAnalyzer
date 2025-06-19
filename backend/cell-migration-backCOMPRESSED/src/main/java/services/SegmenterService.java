package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.FluxSink;

@Service
public class SegmenterService {


	@Value("${output.directory}")
	private String basePath;
	@Value("${generate.segmentResults}")
	private String script_path;
	
	public void createSegmentedResult(FluxSink<String> sink,Long ID,String no_iter,String step_size,String no_box_x,String no_box_y) {
			
		File resultDir = new File(this.basePath, "result" + ID);

	    ProcessBuilder processBuilder = new ProcessBuilder("python3", this.script_path, resultDir.getAbsolutePath(), no_iter,step_size);
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
	
	
	
}
