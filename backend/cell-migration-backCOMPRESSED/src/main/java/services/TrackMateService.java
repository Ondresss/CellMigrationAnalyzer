package services;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TrackMateService {

    @Value("${output.directory}") 
    private String basePath;

    public void saveBothCSVS(MultipartFile spots, MultipartFile tracks, Long ID) throws IOException {
        File resultDir = new File(basePath, "result" + ID + "/TRACKMATE");

        if (!resultDir.exists()) {
            throw new IOException("Failed to create directory: " + resultDir.getAbsolutePath());
        }

        File spotsCSV = new File(resultDir, spots.getOriginalFilename());
        File tracksCSV = new File(resultDir, tracks.getOriginalFilename());

        spots.transferTo(spotsCSV);
        tracks.transferTo(tracksCSV);

        System.out.println("Saved spots CSV: " + spotsCSV.getAbsolutePath());
        System.out.println("Saved tracks CSV: " + tracksCSV.getAbsolutePath());
    }
}
