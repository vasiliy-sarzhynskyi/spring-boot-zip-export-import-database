package com.vsarzhynskyi.export_import_db;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@OpenAPIDefinition(info = @Info(title = "Zip Export/Import Database Data REST API"))
@SpringBootApplication
public class ZipExportImportDataServiceApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ZipExportImportDataServiceApplication.class, args);
    }

}
