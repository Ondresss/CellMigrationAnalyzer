package cz.vsb.fei.cell_migration_back.DTOS;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Comment {
	
	private String author;
	private String text;
	
}
