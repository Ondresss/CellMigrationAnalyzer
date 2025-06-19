package services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cz.vsb.fei.cell_migration_back.DTOS.GeneratedLevelset;

@Service
public class GeneratedLevelsetService {

	@Value("${output.directory}")
	private String basePath;
	
	public List<GeneratedLevelset> getResultLevelsets(Long ID) throws ImageWriteException, IOException, ImageReadException {
			List<GeneratedLevelset> generated_levelsets = new ArrayList<>();
			File resultDir = new File(this.basePath,"result" + ID);
			File dataDir = new File(resultDir,"DATA");
			File[] dataFiles = dataDir.listFiles(File::isFile);
			for(File f : dataFiles ) {
					if(f.getName().endsWith(".tif")) {
							String pngFileName = f.getName().substring(0, f.getName().lastIndexOf(".")) + ".png";
		                    File outPng = new File(resultDir, pngFileName); 
		                    System.out.println("Converting file: " + f.getName());
		                    
		                    BufferedImage image = Imaging.getBufferedImage(f);
		                    Imaging.writeImage(image, outPng, ImageFormats.PNG);
		                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
			                Imaging.writeImage(image, outputStream, ImageFormats.PNG);
			                byte[] pngBytes = outputStream.toByteArray();
			                String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(pngBytes);
			                GeneratedLevelset gen = new GeneratedLevelset(base64Image,f.getName());
			                generated_levelsets.add(gen);
		                    
					}
			}
			
			return generated_levelsets;
	}
	
}
