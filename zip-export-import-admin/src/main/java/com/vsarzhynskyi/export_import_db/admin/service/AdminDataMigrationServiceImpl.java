package com.vsarzhynskyi.export_import_db.admin.service;

import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.vsarzhynskyi.export_import_db.admin.exception.ExportImportDataException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.FileSystemUtils;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

@Slf4j
@AllArgsConstructor
public class AdminDataMigrationServiceImpl implements AdminDataMigrationService {

    private static final String CSV_EXTENSION = ".csv";
    private static final String EXPORTED_ZIP_FILE_NAME = "exported_data";

    private static final String FIRST_TABLE = "first_name";
    private static final String SECOND_TABLE = "second_name";
    private static final String THIRD_TABLE = "third_name";

    private static final String SELECT_ALL_QUERY = "SELECT * FROM `%s`";
    private static final String SELECT_TABLE_METADATA_QUERY = "SELECT * FROM `%s` WHERE 1=2";
    private static final String DELETE_ALL_QUERY = "DELETE FROM `%s`";
    private static final String INSERT_QUERY = "INSERT INTO `%s` VALUES (%s)";
    private static final int INSERT_BATCH_SIZE = 500;
    private static final String BOOLEAN_TRUE_DATABASE_VALUE = "1";

    private static final List<String> APP_TABLES = List.of(FIRST_TABLE, SECOND_TABLE, THIRD_TABLE);
    private static final Map<String, String> TABLE_AND_SELECT_QUERY_PAIRS = APP_TABLES.stream()
            .collect(toUnmodifiableMap(Function.identity(), tableName -> format(SELECT_ALL_QUERY, tableName)));

    private final DataSource dataSource;

    @SneakyThrows
    @Override
    public void exportData(ZipOutputStream zipOutputStream) {
        var exportedDataDirectory = Files.createTempDirectory(EXPORTED_ZIP_FILE_NAME);
        try (var connection = dataSource.getConnection()) {
            TABLE_AND_SELECT_QUERY_PAIRS.forEach((tableName, selectQuery) ->
                    exportTableDataToOutputStream(tableName, selectQuery, exportedDataDirectory, connection, zipOutputStream));
        } finally {
            FileSystemUtils.deleteRecursively(exportedDataDirectory);
        }
        log.info("Export of app data has been completed");
    }

    private void exportTableDataToOutputStream(String tableName, String sqlQuery, Path exportedDataDirectory,
                                               Connection connection, ZipOutputStream zipOutputStream) {
        var path = buildCsvPath(exportedDataDirectory, tableName);
        exportFromDatabaseTable(sqlQuery, connection, path);
        addFileToZip(zipOutputStream, path);
    }

    private Path buildCsvPath(Path parentDirectory, String tableName) {
        return parentDirectory.resolve(format("%s%s", tableName, CSV_EXTENSION));
    }

    @SneakyThrows
    private void addFileToZip(ZipOutputStream zipOutputStream, Path path) {
        zipOutputStream.putNextEntry(new ZipEntry(path.getFileName().toString()));
        var fileInputStream = Files.newInputStream(path);
        IOUtils.copy(fileInputStream, zipOutputStream);
        fileInputStream.close();
        zipOutputStream.closeEntry();
    }

    @SneakyThrows
    private void exportFromDatabaseTable(String sqlQuery, Connection connection, Path path) {
        try (var writer = new CSVWriter(Files.newBufferedWriter(path));
             var preparedStatement = connection.prepareStatement(sqlQuery);
             var resultSet = preparedStatement.executeQuery()) {
            var metaData = resultSet.getMetaData();
            var resultSetFields = getResultSetFields(metaData);
            writer.writeNext(resultSetFields.toArray(String[]::new), true);
            var columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                var sqlRowValues = new String[columnCount];
                for (var i = 1; i <= columnCount; i++) {
                    sqlRowValues[i - 1] = resultSet.getString(i);
                }
                writer.writeNext(sqlRowValues, true);
            }
            writer.flush();
        }
    }

    @SneakyThrows
    private List<String> getResultSetFields(ResultSetMetaData metaData) {
        var columnCount = metaData.getColumnCount();
        var resultSetFields = new ArrayList<String>(columnCount);
        for (var i = 1; i <= columnCount; i++) {
            resultSetFields.add(metaData.getColumnName(i).toLowerCase());
        }
        return resultSetFields;
    }

    @SneakyThrows
    @Override
    public void deleteAllData() {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {

            for (var tableName : Lists.reverse(APP_TABLES)) {
                statement.addBatch(buildDeleteQuery(tableName));
            }
            statement.executeBatch();
            connection.commit();
        }
    }

    private String buildDeleteQuery(String tableName) {
        return format(DELETE_ALL_QUERY, tableName);
    }

    @SneakyThrows
    @Override
    public void importData(ZipInputStream zipInputStream) {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            ZipEntry nextEntry;
            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                var fileName = nextEntry.getName();
                var tableName = fileName.replace(CSV_EXTENSION, StringUtils.EMPTY);
                if (!APP_TABLES.contains(tableName)) {
                    throw new ExportImportDataException(format("Import of unsupported table name [%s] is not allowed", tableName));
                }

                var reader = buildCSVReader(zipInputStream);
                importDataFromCsvReader(tableName, reader, connection);
                zipInputStream.closeEntry();
            }
        }
    }

    // need to use `RFC4180Parser` to correctly handle backslashes inside line values
    private CSVReader buildCSVReader(InputStream inputStream) {
        var rfc4180Parser = new RFC4180ParserBuilder()
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build();
        return new CSVReaderBuilder(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
                .withCSVParser(rfc4180Parser)
                .build();
    }

    @SneakyThrows
    private void importDataFromCsvReader(String tableName, CSVReader reader, Connection connection) {
        var headerLine = reader.readNext();
        var columnCount = headerLine.length;
        log.info("Processing import data for table [{}] with columns {}", tableName, Arrays.toString(headerLine));

        var tableColumnTypes = getTableColumnTypes(tableName, connection);
        var insertQuery = buildInsertQuery(tableName, columnCount);
        try (var preparedStatement = connection.prepareStatement(insertQuery)) {
            String[] csvLine;
            var rowIndex = 0;
            while ((csvLine = reader.readNext()) != null) {
                for (var i = 1; i <= columnCount; i++) {
                    if (tableColumnTypes.get(i - 1) == Types.BIT) {
                        setBooleanPreparedStatementValue(preparedStatement, i, csvLine[i - 1]);
                    } else {
                        preparedStatement.setObject(i, csvLine[i - 1]);
                    }
                }
                preparedStatement.addBatch();
                rowIndex++;
                if (rowIndex % INSERT_BATCH_SIZE == 0) {
                    log.info("Executing insert batch for table [{}] and row index [{}]", tableName, rowIndex);
                    preparedStatement.executeBatch();
                }
            }
            preparedStatement.executeBatch();
            connection.commit();
        }
    }

    @SneakyThrows
    private void setBooleanPreparedStatementValue(PreparedStatement preparedStatement, int parameterIndex, String value) {
        var booleanValue = ofNullable(value)
                .map(BOOLEAN_TRUE_DATABASE_VALUE::equals)
                .orElse(null);
        if (nonNull(booleanValue)) {
            preparedStatement.setBoolean(parameterIndex, booleanValue);
        } else {
            preparedStatement.setObject(parameterIndex, null);
        }
    }

    private String buildInsertQuery(String tableName, int columnCount) {
        var insertValuesClause = Stream.generate(() -> "?").limit(columnCount).collect(joining(", "));
        return format(INSERT_QUERY, tableName, insertValuesClause);
    }

    @SneakyThrows
    private List<Integer> getTableColumnTypes(String tableName, Connection connection) {
        var selectTableMetadata = format(SELECT_TABLE_METADATA_QUERY, tableName);
        try (var preparedStatement = connection.prepareStatement(selectTableMetadata);
             var resultSet = preparedStatement.executeQuery()) {
            var metaData = resultSet.getMetaData();
            var columnCount = metaData.getColumnCount();
            var resultSetTypes = new ArrayList<Integer>(columnCount);
            for (var i = 1; i <= columnCount; i++) {
                resultSetTypes.add(metaData.getColumnType(i));
            }
            return resultSetTypes;
        }
    }

}
