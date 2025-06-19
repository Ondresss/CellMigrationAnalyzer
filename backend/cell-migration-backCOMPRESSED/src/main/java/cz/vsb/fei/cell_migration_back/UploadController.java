package cz.vsb.fei.cell_migration_back;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cz.vsb.fei.cell_migration_back.utilities.RandomIDGenerator;
import services.BrightFieldService;
import services.DAPIService;
import services.DirectoryService;
import services.ResultService;

@CrossOrigin(origins = "http://localhost:8081") 
@RestController
public class UploadController {

	@Autowired
	private DirectoryService dir_service;
	@Autowired
	private ResultService res_service;
	@Autowired
	private BrightFieldService bright_service;
	@Autowired
	private DAPIService dapi_service;


	@PostMapping("/uploadBrightField")
	public ResponseEntity<Long> handleBrightFieldUpload(@RequestParam("files") List<MultipartFile> files,@RequestParam("ID") Long id) {
		if(id == null) {
			throw new RuntimeException("ID is null");
		}
		try {
			this.bright_service.saveBrightFields("result" + id, files);
			return ResponseEntity.ok(id);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}

	}

	@PostMapping("/existingBrightFieldResult")
	public ResponseEntity<Void> handleBrightFieldExisting(@RequestParam("files") List<MultipartFile> files,@RequestParam("ID") Long ID) {
		try {
			this.bright_service.saveBrightFields("result" + ID, files);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/uploadDapiNEW")
	public ResponseEntity<Long> handleDAPIUpload(@RequestParam("files") List<MultipartFile> files) {
		Long id = RandomIDGenerator.generateID();
		this.dir_service.createNewResultDirectory(id);
		try {
			this.dapi_service.saveDapiFiles(files, id);
			return ResponseEntity.ok(id);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.noContent().build();
		}

	}
	@PostMapping("/uploadDapiExisting")
	public ResponseEntity<Void> handleDAPIUploadExisting(@RequestParam("files") List<MultipartFile> files,@RequestParam("ID") Long ID) {

		try {
			this.dapi_service.saveDapiFiles(files, ID);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}



}
