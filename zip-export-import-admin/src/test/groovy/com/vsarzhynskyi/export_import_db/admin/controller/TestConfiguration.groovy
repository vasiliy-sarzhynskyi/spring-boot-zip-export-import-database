package com.vsarzhynskyi.export_import_db.admin.controller

import com.vsarzhynskyi.export_import_db.admin.exception.ExportImportDataException
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

import static org.springframework.http.HttpStatus.BAD_REQUEST

@Configuration
@ComponentScan(["com.vsarzhynskyi.export_import_db.admin.controller"])
class TestConfiguration {

    @Bean
    @ConditionalOnMissingBean(annotation = ControllerAdvice.class)
    AdminExceptionHandlingControllerAdvice adminExceptionHandlingControllerAdvice() {
        return new AdminExceptionHandlingControllerAdvice()
    }


    @RestControllerAdvice
    static class AdminExceptionHandlingControllerAdvice {
        @ExceptionHandler([ExportImportDataException.class])
        @ResponseStatus(BAD_REQUEST)
        String handleValidationException(Exception e) {
            String errorMessage = String.format("App validation exception: %s.", e.getMessage())
            e.printStackTrace()
            return errorMessage
        }
    }

}
