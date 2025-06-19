package cz.vsb.fei.cell_migration_back;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cz.vsb.fei.cell_migration_back.DTOS.GeneratedLevelset;
import lombok.extern.log4j.Log4j2;
import services.GeneratedLevelsetService;

@RestController
@CrossOrigin(origins = "http://localhost:8081") 
@Log4j2
public class GeneratedLevelsetController {

	@Autowired
	private GeneratedLevelsetService gen_service;
	
	@PostMapping("/getLevelsets")
	public ResponseEntity<List<GeneratedLevelset>> getGeneratedLevelsets(@RequestParam("ID") Long ID) {
		try {
			List<GeneratedLevelset> gen_levelsets = this.gen_service.getResultLevelsets(ID);
			if(gen_levelsets.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(gen_levelsets);
		} catch (ImageWriteException | ImageReadException | IOException e) {
			log.error("Error retrieving level sets for ID: " + ID, e);
			return ResponseEntity.internalServerError().body(Collections.EMPTY_LIST);
		}
		
	}
	
	
}
