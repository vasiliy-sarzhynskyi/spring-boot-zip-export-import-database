package com.vsarzhynskyi.export_import_db.functional

import com.playtika.test.mariadb.EmbeddedMariaDBBootstrapConfiguration
import com.playtika.test.mariadb.EmbeddedMariaDBDependenciesAutoConfiguration
import com.vsarzhynskyi.export_import_db.ZipExportImportDataServiceApplication
import io.restassured.RestAssured
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = [
                DataSourceAutoConfiguration.class,
                EmbeddedMariaDBDependenciesAutoConfiguration.class,
                EmbeddedMariaDBBootstrapConfiguration.class,
                LiquibaseAutoConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class,
                ZipExportImportDataServiceApplication.class,
                JpaRepositoriesAutoConfiguration.class
        ]
)
@ActiveProfiles("test")
abstract class BaseMediumTest extends Specification {

    @LocalServerPort
    int port

    def setup() {
        RestAssured.port = port
    }

}