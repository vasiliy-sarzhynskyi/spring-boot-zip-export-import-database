package com.vsarzhynskyi.export_import_db.functional

import com.vsarzhynskyi.export_import_db.jpa.entity.FirstEntity
import com.vsarzhynskyi.export_import_db.jpa.repository.FirstRepository
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired

import java.nio.file.Files
import java.nio.file.Path

import static io.restassured.RestAssured.given

class AdminDataMigrationMediumTest extends BaseMediumTest {

    private static final ADMIN_EXPORT_URL = 'admin/export'
    private static final ADMIN_DELETE_ALL_URL = 'admin/delete-all-data'
    private static final ADMIN_IMPORT_URL = 'admin/import'

    @Autowired
    private FirstRepository firstRepository

    def "should export, delete all and import back migration data"() {
        given:
        def zippedExportedDataPath = Path.of('src/test/resources/exported_data.zip')

        when:
        given()
                .when()
                .delete(ADMIN_DELETE_ALL_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)

        and:
        createInitialData()

        then:
        fetchData().size() == 2

        when:
        def responseZippedStream = given()
                .when()
                .get(ADMIN_EXPORT_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .asInputStream()
        Files.copy(responseZippedStream, zippedExportedDataPath)

        and:
        given()
                .when()
                .delete(ADMIN_DELETE_ALL_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)

        then:
        fetchData().size() == 0

        when:
        given()
                .when()
                .multiPart('uploadedFile', zippedExportedDataPath.toFile())
                .post(ADMIN_IMPORT_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)

        then:
        fetchData().size() == 2

        cleanup:
        Files.delete(zippedExportedDataPath)
        given()
                .when()
                .delete(ADMIN_DELETE_ALL_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
    }

    private void createInitialData() {
        def entityToSave1 = FirstEntity.builder()
                .description('Spring Boot')
                .build()
        def entityToSave2 = FirstEntity.builder()
                .description('Spring Cloud')
                .build()
        firstRepository.saveAll([entityToSave1, entityToSave2])
    }

    private List<FirstEntity> fetchData() {
        firstRepository.findAll()
    }

}
