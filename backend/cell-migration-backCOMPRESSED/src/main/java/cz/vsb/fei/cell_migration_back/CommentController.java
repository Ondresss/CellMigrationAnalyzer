package cz.vsb.fei.cell_migration_back;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cz.vsb.fei.cell_migration_back.DTOS.Comment;
import services.CommentService;

@CrossOrigin(origins = "http://localhost:8081") 
@RestController
public class CommentController {

	@Autowired
	CommentService comment_service;
	
	
	@GetMapping("/resultComment")
	public ResponseEntity<List<Comment>> getResultComments(@RequestParam("ID") Long ID) {
		
		List<Comment> comments = this.comment_service.getResultComments(ID);
		if(comments.isEmpty() || comments == null) {
			return ResponseEntity.noContent().build();
		}
		
		return ResponseEntity.ok(comments);
		
	}
	@GetMapping("/addComment")
	public ResponseEntity<Void> addComent(@RequestParam("ID") Long ID,@RequestParam("author") String author,@RequestParam("text") String text) {
		Comment com = new Comment(author,text);
		this.comment_service.saveComment(ID, com);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);	
	}
	
}
