package cz.vsb.fei.cell_migration_back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import services.BrightFieldService;
import services.SegmenterService;

@CrossOrigin(origins = "http://localhost:8081") 
@RestController
public class ExecuteController {

	@Autowired
	private BrightFieldService br_service;
	@Autowired
	private SegmenterService segmenter_service;
	
	
	@GetMapping(value = "/api/generateLevelSets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> generateLevelSets(@RequestParam("no_iter") String no_iter,@RequestParam("ID") Long ID) {
		return Flux.create(sink->{
			this.br_service.generateLevelsets(no_iter, ID, sink);
		});
	}
	@GetMapping(value = "/api/segmentResult", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> segmentResult(@RequestParam("ID") Long ID,
			@RequestParam("no_iter") String no_iter,@RequestParam("step_size") String step_size,@RequestParam("no_box_x") String no_box_x,@RequestParam("no_box_y") String no_box_y) {
		if(ID == null) {
				throw new RuntimeException("ID is nulll");
		}
		return Flux.create(sink->{
			this.segmenter_service.createSegmentedResult(sink, ID, no_iter, step_size, no_box_x, no_box_y);
		});
	}
	
}
