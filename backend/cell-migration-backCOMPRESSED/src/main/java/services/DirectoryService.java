package services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DirectoryService {
	
		@Value("${output.directory}")
		private String basePath;
		
		public void createNewResultDirectory(Long id) {
			Path resultDir = Paths.get(this.basePath,"result"+id);
			try {
		         Files.createDirectories(resultDir);
		         System.out.println("Directory created: " + resultDir);
		         
		         Path dataPath = resultDir.resolve("DATA");
	             Files.createDirectories(dataPath);
	             System.out.println("Subdirectory created: " + dataPath);
	                
	             Path brightFieldPath = resultDir.resolve("BrightField");
	             Files.createDirectories(brightFieldPath);
	             System.out.println("Subdirectory created: " + brightFieldPath);
	                
	             Path csvPath = resultDir.resolve("CSV");
	             Files.createDirectories(csvPath);
	             System.out.println("Subdirectory created: " + csvPath);
	             
	             Path trackPath = resultDir.resolve("TRACKS");
	             Files.createDirectories(trackPath);
	             System.out.println("Subdirectory created: " + trackPath);
	             
	             Path dapiPath = resultDir.resolve("DAPI");
	             Files.createDirectories(dapiPath);
	             System.out.println("Subdirectory created: " + dapiPath);
	             
	             Path trackmatePath = resultDir.resolve("TRACKMATE");
	             Files.createDirectories(trackmatePath);
	             System.out.println("Subdirectory created: " + trackmatePath);
	             
	             Path commentFilePath = resultDir.resolve("comment.txt");
	             Files.createFile(commentFilePath);
	             
		    } catch (IOException e) {
		    	
		         System.err.println("Error creating directory: " + e.getMessage());
		    }
		}
	
}
