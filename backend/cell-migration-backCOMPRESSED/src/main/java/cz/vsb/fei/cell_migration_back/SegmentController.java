package cz.vsb.fei.cell_migration_back;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import services.TrackMateService;

@CrossOrigin(origins = "http://localhost:8081") 
@RestController
public class SegmentController {
	
	
	@Autowired
	private TrackMateService trackmate_service;
	
	
	@PostMapping("/api/prepareSegmentation")
	public ResponseEntity<Void> prepareForSegmentation(
			@RequestParam("spots") MultipartFile spots
			,@RequestParam("tracks") MultipartFile tracks,@RequestParam("ID") Long ID) {
		
		try {
			this.trackmate_service.saveBothCSVS(spots, tracks, ID);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	
}
