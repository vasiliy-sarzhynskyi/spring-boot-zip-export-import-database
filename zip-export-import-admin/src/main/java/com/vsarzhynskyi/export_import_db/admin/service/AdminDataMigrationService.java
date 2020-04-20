package com.vsarzhynskyi.export_import_db.admin.service;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public interface AdminDataMigrationService {

    void exportData(ZipOutputStream zipOutputStream);
    void deleteAllData();
    void importData(ZipInputStream zipInputStream);

}
