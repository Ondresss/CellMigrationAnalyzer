package cz.vsb.fei.cell_migration_back;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cz.vsb.fei.cell_migration_back.DTOS.Result;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import services.ResultService;

@CrossOrigin(origins = "http://localhost:8081") 
@RestController
@NoArgsConstructor
@Log4j2
public class StartController {

	
	@Autowired
	private ResultService result_service;
	
	
	public StartController(ResultService result_service) {
		this.result_service = result_service;
	}
	
	
	@DeleteMapping("/removeResult")
	public ResponseEntity<Void> removeResult(@RequestParam("id") Integer id) {
		String filename = "result" + id.toString();
		try {
			this.result_service.removeResult(filename);
			System.out.println("Result delted: " + id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);	
			
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@GetMapping("/resultIDS")
	public ResponseEntity<List<Long>> getResultIDS() {
		List<Long> ids = this.result_service.getAllResultIDs();
		if(ids != null || !ids.isEmpty()) {
			return ResponseEntity.ok(ids);
		} 
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/resultData")
	public ResponseEntity<List<String>> getResultData(@RequestBody Map<String, Long> request) {
	    Long id = request.get("ID"); 

	    if (id == null) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	    }

	    List<String> filesData = new ArrayList<>();
	    try {
	        List<File> files = this.result_service.getResultData(id);
	        if (files == null || files.isEmpty()) {
	            return ResponseEntity.ok(Collections.emptyList());
	        }

	        for (File file : files) {
	            byte[] data = Files.readAllBytes(file.toPath());
	            String base64 = Base64.getEncoder().encodeToString(data);
	            filesData.add("data:image/png;base64," + base64); 
	        }

	        return ResponseEntity.ok(filesData);
	    } catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}

	
	@GetMapping("/getDate")
	public ResponseEntity<LocalDateTime> getResultDate(@RequestParam("result") String result) {
		
			LocalDateTime tm = this.result_service.getResultDate(result);
			if(tm != null) {
				return ResponseEntity.ok(tm);
			}
			return ResponseEntity.notFound().build();
			
		
	}
	
	@PostMapping("/data")
	public String uploadBrightField(@RequestParam("file") MultipartFile file) {
		log.info("File uploaded");
		return "File recieved";
	}
	@GetMapping("/results")
	public ResponseEntity<List<Result>> getAllResults() {
		
		try {
			List<Result> results = this.result_service.processAllResults();
			if(!results.isEmpty() && results != null) {
				return ResponseEntity.ok(results);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (ImageReadException | ImageWriteException | IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
		
	}
	@GetMapping("/resultsCSV")
	public ResponseEntity<List<String>> getAllCSVNames() {
		List<String> files;
		try {
			files = this.result_service.getSavedResultsCSV();
			return ResponseEntity.ok(files);
		} catch (ImageWriteException | ImageReadException | IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
	}
	@GetMapping("/downloadCSV/{filename}/{id}")
	public ResponseEntity<Resource> getAllCSVNames(@PathVariable String filename,@PathVariable String id) {
		try {
			return this.result_service.getCSVFile(id, filename);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
		
	}
		
		
	
}
