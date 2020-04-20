package com.vsarzhynskyi.export_import_db.functional

import org.apache.http.HttpStatus
import org.hamcrest.Matchers

import static io.restassured.RestAssured.when

class HealthCheckMediumTest extends BaseMediumTest {

    def "should load application and return health check"() {
        expect:
        when().get("/health")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("status", Matchers.is("UP"))

        and:
        when().get("/swagger-ui.html")
                .then()
                .statusCode(HttpStatus.SC_OK)
    }
}
