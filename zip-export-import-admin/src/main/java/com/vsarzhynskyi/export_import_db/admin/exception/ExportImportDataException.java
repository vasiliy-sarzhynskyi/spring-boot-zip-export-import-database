package com.vsarzhynskyi.export_import_db.admin.exception;

public class ExportImportDataException extends RuntimeException {

    public ExportImportDataException(String message) {
        super(message);
    }

    public ExportImportDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
