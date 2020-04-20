package com.vsarzhynskyi.export_import_db.admin.configuration;

import com.vsarzhynskyi.export_import_db.admin.service.AdminDataMigrationService;
import com.vsarzhynskyi.export_import_db.admin.service.AdminDataMigrationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AdminConfiguration {

    @Bean
    public AdminDataMigrationService adminDataMigrationService(DataSource dataSource) {
        return new AdminDataMigrationServiceImpl(dataSource);
    }

}
