package com.vsarzhynskyi.export_import_db.admin.controller;

import com.vsarzhynskyi.export_import_db.admin.service.AdminDataMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("admin")
@Slf4j
@AllArgsConstructor
public class AdminExportController {

    private static final String APP_ZIP_MEDIA_TYPE = "application/zip";
    private static final String DISPOSITION_HEADER_KEY = "Content-Disposition";
    private static final String DISPOSITION_HEADER_VALUE = "attachment; filename=\"exported_data.zip\"";

    private final AdminDataMigrationService adminService;

    @GetMapping(value = "/export", produces = APP_ZIP_MEDIA_TYPE)
    @Operation(summary = "Export database data", description = "Export database data as zip archive from all tables")
    public void exportDataAsZip(HttpServletResponse response) throws IOException {
        log.info("Exporting database data");
        response.setContentType(APP_ZIP_MEDIA_TYPE);
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader(DISPOSITION_HEADER_KEY, DISPOSITION_HEADER_VALUE);

        try (var zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            adminService.exportData(zipOutputStream);
        }
    }

}
