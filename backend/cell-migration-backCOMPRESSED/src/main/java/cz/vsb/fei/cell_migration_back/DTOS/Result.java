package cz.vsb.fei.cell_migration_back.DTOS;

import java.io.File;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class Result {
	
	private List<String> csvFileNames;	
	private List<String> files64;
	private List<String> fileNames;
	
		
}
