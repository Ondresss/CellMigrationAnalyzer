package cz.vsb.fei.cell_migration_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "cz.vsb.fei.cell_migration_back", "services" })
public class CellMigrationBackApplication {
	public static void main(String[] args) {
		SpringApplication.run(CellMigrationBackApplication.class, args);
	}

}
