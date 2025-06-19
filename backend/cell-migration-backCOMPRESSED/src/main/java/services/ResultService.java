package services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cz.vsb.fei.cell_migration_back.DTOS.Result;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@NoArgsConstructor
@Data
@Log4j2
public class ResultService {
	
	@Value("${output.directory}")
	private String basePath;
	
	public void removeResult(String filename) throws IOException {
		  Path filePath = Paths.get(this.basePath, filename);
		  if (!Files.exists(filePath)) {
			    throw new FileNotFoundException("File not found: " + filePath);
		  }
		  Files.walk(filePath)
          .sorted(Comparator.reverseOrder()) 
          .forEach(path -> {
                 try {
                     Files.delete(path); 
                     System.out.println("Deleted: " + path);
                 } catch (IOException e) {
                     System.err.println("Failed to delete: " + path + " due to: " + e.getMessage());
                 }
          });
          
	}
	
	
	public LocalDateTime getResultDate(String filename) {
		 Path filePath = Paths.get(this.basePath, filename);
		 try {
	            BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
	            LocalDateTime creationDateTime = Instant.ofEpochMilli(attributes.creationTime().toMillis())
	                                                    .atZone(ZoneId.systemDefault())
	                                                    .toLocalDateTime();
	            System.out.println("Directory creation date and time: " + creationDateTime);
	            return creationDateTime;
	        } catch (IOException e) {
	            System.err.println("Error retrieving directory attributes: " + e.getMessage());
	            return null;
	        }
	}
		
	public List<Long> getAllResultIDs() {
	    File baseDir = new File(this.basePath);

	    if (!baseDir.exists() || !baseDir.isDirectory()) {
	        log.error("Base directory does not exist or is not a directory: " + this.basePath);
	        return Collections.emptyList();
	    }

	    List<Long> resultIDs = new ArrayList<>();
	    File[] directories = baseDir.listFiles(File::isDirectory);

	    if (directories != null) {
	        for (File dir : directories) {
	            String dirName = dir.getName();
	            if (dirName.startsWith("result")) {
	                try {
	       
	                    String idStr = dirName.substring(6); 
	                    Long id = Long.parseLong(idStr);
	                    resultIDs.add(id);
	                } catch (NumberFormatException e) {
	                    log.warn("Directory does not have a valid numeric ID: " + dirName);
	                }
	            }
	        }
	    } else {
	        log.warn("No subdirectories found in base directory: " + this.basePath);
	    }

	    return resultIDs;
	}

	public List<File> getResultData(Long resultID) {
	
	    if (resultID == null || resultID <= 0) {
	        log.error("Invalid resultID: " + resultID);
	        return Collections.emptyList();
	    }

	    File resultDir = new File(this.basePath, "result" + resultID + "" + "/DATA");
	    if (!resultDir.exists() || !resultDir.isDirectory()) {
	        log.error("Result directory does not exist or is not a directory: " + resultDir.getPath());
	        return Collections.emptyList();
	    }

	    List<File> output = new ArrayList<>();
	    try {
	        File[] files = resultDir.listFiles(File::isFile);
	        if (files != null && files.length > 0) {
	            for (File file : files) {
	                if (file.getName().toLowerCase().endsWith(".tif")) {
	                    String pngFileName = file.getName().substring(0, file.getName().lastIndexOf(".")) + ".png";
	                    File outPng = new File(resultDir, pngFileName); 
	                    System.out.println("Converting file: " + file.getName());
	                    
	                    BufferedImage image = Imaging.getBufferedImage(file);
	                    Imaging.writeImage(image, outPng, ImageFormats.PNG);

	                    output.add(outPng);
	                    System.out.println("Converted and saved: " + outPng.getPath());
	                }
	            }
	        } else {
	            System.out.println("No files found in the directory: " + resultDir.getPath());
	        }
	    } catch (IOException | ImageWriteException | ImageReadException e) {
	        log.error("Error processing files in directory: " + resultDir.getPath(), e);
	    }

	    return output;
	}
	
	public List<Result> processAllResults() throws IOException, ImageReadException, ImageWriteException {
	    File baseDir = new File(this.basePath);
	    List<Result> allResults = new ArrayList<>();

	    if (!baseDir.exists() || !baseDir.isDirectory()) {
	        System.err.println("Base directory does not exist or is not a directory.");
	        return allResults;
	    }

	    File[] resultDirs = baseDir.listFiles(dir -> dir.isDirectory() && dir.getName().startsWith("result"));
	    if (resultDirs != null) {
	        for (File resultDir : resultDirs) {
	            Result result = processTracks(resultDir);
	            allResults.add(result);
	        }
	    }

	    return allResults;
	}

	public Result processTracks(File resultDir) throws IOException, ImageReadException, ImageWriteException {
	    List<String> base64Images = new ArrayList<>();
	    List<String> csvFileNames = new ArrayList<>();
	    List<String> resultFileNames = new ArrayList<>();

	    File tracksDir = new File(resultDir, "TRACKS");
	    if (tracksDir.exists() && tracksDir.isDirectory()) {
	        File[] tifFiles = tracksDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".tif"));
	        if (tifFiles != null) {
	            for (File tifFile : tifFiles) {
	                BufferedImage image = Imaging.getBufferedImage(tifFile);
	                resultFileNames.add(tifFile.getName());
	                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
	                    Imaging.writeImage(image, outputStream, ImageFormats.PNG);
	                    byte[] pngBytes = outputStream.toByteArray();
	                    String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(pngBytes);
	                    base64Images.add(base64Image);
	                }
	            }
	        }
	    }

	    File csvDir = new File(resultDir, "CSV");
	    if (csvDir.exists() && csvDir.isDirectory()) {
	        File[] csvFiles = csvDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
	        if (csvFiles != null) {
	            for (File csvFile : csvFiles) {
	                csvFileNames.add(csvFile.getName());
	            }
	        }
	    }

	  
	    return new Result(csvFileNames,base64Images,resultFileNames);
	}
	
	public ResponseEntity<Resource> getCSVFile(String resultId, String csvFileName) throws IOException {
	    // Construct the result directory based on the provided ID
	    File resultDir = new File(this.basePath, "result" + resultId);

	    // Check if the result directory exists
	    if (!resultDir.exists() || !resultDir.isDirectory()) {
	        System.err.println("Result directory does not exist: " + resultDir.getPath());
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 if directory not found
	    }

	    File csvDir = new File(resultDir, "CSV");
	    if (!csvDir.exists() || !csvDir.isDirectory()) {
	        System.err.println("CSV directory does not exist: " + csvDir.getPath());
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); 
	    }

	    File csvFile = new File(csvDir, csvFileName);
	    if (!csvFile.exists() || !csvFile.isFile()) {
	        System.err.println("CSV file not found: " + csvFile.getPath());
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); 
	    }

	    Resource resource = new FileSystemResource(csvFile);

	    // Return the file as a response with appropriate headers
	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csvFile.getName() + "\"")
	            .header(HttpHeaders.CONTENT_TYPE, "text/csv")
	            .body(resource);
	}


	
	
	
	public List<File> getSavedResults() throws IOException, ImageWriteException, ImageReadException {
	    File result_dir = new File(this.basePath);
	    if (!result_dir.exists() || !result_dir.isDirectory()) {
	        log.error("Result directory is not available");
	        return Collections.EMPTY_LIST;
	    }

	    List<File> output = new ArrayList<>();
	    File[] resultDirectories = result_dir.listFiles(File::isDirectory);

	    if (resultDirectories != null) {
	        for (File resultDir : resultDirectories) {
	            // Skip the DATA directory
	            if ("DATA".equalsIgnoreCase(resultDir.getName())) {
	                System.out.println("Skipping directory: " + resultDir.getName());
	                continue;
	            }
	            if ("CSV".equalsIgnoreCase(resultDir.getName())) {
	                System.out.println("Skipping directory: " + resultDir.getName());
	                continue;
	            }

	            System.out.println("Processing directory: " + resultDir.getName());
	            File[] files = resultDir.listFiles(File::isFile);

	            if (files != null && files.length > 0) {
	                for (File file : files) {
	                    if (file.getName().toLowerCase().endsWith(".tif")) {
	                        String pngFileName = file.getName().substring(0, file.getName().lastIndexOf(".")) + ".png";
	                        File outPng = new File(this.basePath, pngFileName);
	                        System.out.println(file.getName());
	                        BufferedImage image = Imaging.getBufferedImage(file);
	                        Imaging.writeImage(image, outPng, ImageFormats.PNG);
	                        output.add(outPng);
	                        System.out.println("Wrote " + outPng.getName());
	                        System.out.println("File path: " + resultDir.getPath());
	                    }
	                }
	            } else {
	                System.out.println("No files found in " + resultDir.getPath());
	                return Collections.EMPTY_LIST;
	            }
	        }
	    } else {
	        System.out.println("No subdirectories found.");
	        return Collections.EMPTY_LIST;
	    }

	    return output;
	}

	
	
	
	public List<String> getSavedResultsCSV() throws ImageWriteException, IOException, ImageReadException {
		File result_dir = new File(this.basePath);
		if (!result_dir.exists() || !result_dir.isDirectory()) {
	          	log.error("Result directory is not available");
	            return Collections.EMPTY_LIST;
	    }
		List<String> output = new ArrayList<String>();
		File[] resultDirectories = result_dir.listFiles(File::isDirectory);
			
	    if (resultDirectories != null) {
	        for (File resultDir : resultDirectories) {
	                System.out.println("Directory: " + resultDir.getName());
	                File[] files = resultDir.listFiles(File::isFile);
	                if (files != null && files.length > 0) {
	                    for (File file : files) {
	                    		if(file.getName().toLowerCase().endsWith(".csv")) {
	                    			System.out.println("Found " + file.getName());
		                    		output.add(file.getName());
	                    		}
	                } 
	              } else {
	                    System.out.println("    No files found in " + resultDir.getPath());
	                    return Collections.EMPTY_LIST;
	                }
	            }
	        } else {
	            System.out.println("No subdirectories found.");
	            return Collections.EMPTY_LIST;
	    }
		 
	    return output;	
	}
	public File getConcreteCSV(String filename) throws ImageWriteException, IOException, ImageReadException {
		File result_dir = new File(this.basePath);
		if (!result_dir.exists() || !result_dir.isDirectory()) {
	          	log.error("Result directory is not available");
	            return null;
	    }
		File outputCSV = null;
		File[] resultDirectories = result_dir.listFiles(File::isDirectory);
			
	    if (resultDirectories != null) {
	        for (File resultDir : resultDirectories) {
	                System.out.println("Directory: " + resultDir.getName());
	                File[] files = resultDir.listFiles(File::isFile);
	                if (files != null && files.length > 0) {
	                    for (File file : files) {
	                    		if(file.getName().toLowerCase().endsWith(".csv")) {
	                    			System.out.println("Found " + file.getName());
		                    		outputCSV = file;
	                    		}
	                } 
	              } else {
	                    System.out.println("    No files found in " + resultDir.getPath());
	                    return null;
	                }
	            }
	        } else {
	            System.out.println("No subdirectories found.");
	            return null;
	    }
		 
	    return outputCSV;	
	}	
	
}
