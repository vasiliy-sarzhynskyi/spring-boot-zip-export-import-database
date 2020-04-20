package com.vsarzhynskyi.export_import_db.controller.advice;

import com.vsarzhynskyi.export_import_db.admin.exception.ExportImportDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlingControllerAdvice {

    @ExceptionHandler(ExportImportDataException.class)
    @ResponseStatus(BAD_REQUEST)
    String handleValidationException(Exception e) {
        String errorMessage = String.format("App validation exception: %s.", e.getMessage());
        e.printStackTrace();
        return errorMessage;
    }

}
