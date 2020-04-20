package com.vsarzhynskyi.export_import_db.admin.controller;

import com.vsarzhynskyi.export_import_db.admin.exception.ExportImportDataException;
import com.vsarzhynskyi.export_import_db.admin.service.AdminDataMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.zip.ZipInputStream;

import static java.lang.String.format;

@RestController
@RequestMapping("admin")
@Slf4j
@AllArgsConstructor
@ConditionalOnExpression("${admin.importController.enabled:false}")
public class AdminImportController {

    private static final String ZIP_EXTENSION = ".zip";

    private final AdminDataMigrationService adminService;

    @DeleteMapping("/delete-all-data")
    @Operation(summary = "Delete all database data", description = "Delete data from all tables")
    public void deleteAllData() {
        log.warn("Deleting all app data");
        adminService.deleteAllData();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import database data", description = "Import database data from zip archive")
    public void importZippedData(@RequestPart("uploadedFile") MultipartFile uploadedFile) throws IOException {
        var uploadedFileName = uploadedFile.getOriginalFilename();
        log.warn("Importing data from file [{}]", uploadedFileName);
        validateUploadedFile(uploadedFile);

        try (var zipInputStream = new ZipInputStream(uploadedFile.getInputStream())) {
            adminService.importData(zipInputStream);
        }
    }

    private void validateUploadedFile(MultipartFile uploadedFile) {
        var uploadedFileName = uploadedFile.getOriginalFilename();
        if (uploadedFile.isEmpty() || !uploadedFileName.endsWith(ZIP_EXTENSION)) {
            var errorMessage = format("Failed to upload file [%s] because the uploadedFile is empty or not .zip file", uploadedFileName);
            log.error(errorMessage);
            throw new ExportImportDataException(errorMessage);
        }
    }

}
