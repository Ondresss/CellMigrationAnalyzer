package services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cz.vsb.fei.cell_migration_back.DTOS.Comment;
import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class CommentService {
	
	@Value("${output.directory}")
	private String basePath;
	
	public List<Comment> getResultComments(Long id) {
	    File resultDir = new File(this.basePath, "result" + id);
	    File commentFile = new File(resultDir, "comment.txt");
	    BufferedReader buffReader = null;
	    List<Comment> comments = new ArrayList<>();

	    try {
	        buffReader = new BufferedReader(new FileReader(commentFile));
	        String line;
	        
	        while ((line = buffReader.readLine()) != null) {
	            if (line.trim().isEmpty()) {
	                continue;
	            }

	            String author = line.trim();
	            
	            StringBuilder finalText = new StringBuilder();
	            while ((line = buffReader.readLine()) != null && !line.trim().isEmpty()) {
	                finalText.append(line).append("\n");
	            }
	            
	            if (finalText.length() == 0) {
	                System.err.println("Text for comment by " + author + " is missing or invalid.\n");
	                continue; 
	            }
	            
	            comments.add(new Comment(author, finalText.toString().trim()));
	        }
	        
	        return comments;
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	        try {
	            if (buffReader != null) {
	                buffReader.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public void saveComment(Long id, Comment comment) {
	    File resultDir = new File(this.basePath, "result" + id);
	    File commentFile = new File(resultDir, "comment.txt");

	    if (!resultDir.exists()) {
	        resultDir.mkdirs();
	    }

	    BufferedWriter bufWriter = null;
	    try {
	        bufWriter = new BufferedWriter(new FileWriter(commentFile, true)); 
	        bufWriter.newLine(); 
	        bufWriter.write(comment.getAuthor());
	        bufWriter.newLine();
	        bufWriter.write(comment.getText());
	        bufWriter.newLine(); 
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (bufWriter != null) {
	            try {
	                bufWriter.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}


	
	
}
