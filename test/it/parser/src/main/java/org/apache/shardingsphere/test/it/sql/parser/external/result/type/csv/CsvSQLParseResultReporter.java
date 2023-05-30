/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.it.sql.parser.external.result.type.csv;

import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.shardingsphere.test.it.sql.parser.external.result.SQLParseResultReporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * SQL parse result reporter for CSV.
 */
public final class CsvSQLParseResultReporter implements SQLParseResultReporter {
    
    private final CSVPrinter printer;
    
    @SneakyThrows(IOException.class)
    public CsvSQLParseResultReporter(final String databaseType, final String resultPath) {
        File csvFile = new File(resultPath + databaseType + "-result.csv");
        printHeader(csvFile);
        printer = new CSVPrinter(Files.newBufferedWriter(Paths.get(csvFile.toURI()), StandardOpenOption.APPEND), CSVFormat.DEFAULT.builder().setSkipHeaderRecord(true).build());
    }
    
    @SneakyThrows(IOException.class)
    private void printHeader(final File csvFile) {
        if (csvFile.exists()) {
            return;
        }
        try (
                CSVPrinter csvHeaderPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(csvFile.toURI())), CSVFormat.DEFAULT.builder().setSkipHeaderRecord(false).build())) {
            csvHeaderPrinter.printRecord("SQLCaseId", "DatabaseType", "Result", "SQL");
            csvHeaderPrinter.flush();
        }
    }
    
    /**
     * Print result.
     * 
     * @param sqlCaseId SQL case ID
     * @param databaseType database type
     * @param isSuccess whether success
     * @param sql SQL
     */
    @SneakyThrows(IOException.class)
    @Override
    public void printResult(final String sqlCaseId, final String databaseType, final boolean isSuccess, final String sql) {
        printer.printRecord(sqlCaseId, databaseType, isSuccess ? "success" : "failed", sql);
        printer.flush();
    }
    
    @Override
    public void close() throws IOException {
        printer.close();
    }
}
