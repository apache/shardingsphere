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

package org.apache.shardingsphere.test.integration.sql.parser.external.result.type.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.shardingsphere.test.integration.sql.parser.external.env.SQLParserExternalITEnvironment;
import org.apache.shardingsphere.test.integration.sql.parser.external.result.SQLParseResultReporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *  SQL parse result reporter for CSV.
 */
public final class CsvSQLParseResultReporter implements SQLParseResultReporter {
    
    private final CSVPrinter printer;
    
    public CsvSQLParseResultReporter(final String databaseType) {
        try {
            File csvFile = new File(SQLParserExternalITEnvironment.getInstance().getResultPath() + databaseType + "-result.csv");
            printHeader(csvFile);
            printer = new CSVPrinter(new FileWriter(csvFile, true), CSVFormat.DEFAULT.builder().setSkipHeaderRecord(true).build());
        } catch (final IOException ex) {
            throw new RuntimeException("Create CSV file failed.", ex);
        }
    }
    
    private void printHeader(final File csvFile) {
        if (csvFile.exists()) {
            return;
        }
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(csvFile), CSVFormat.DEFAULT.builder().setSkipHeaderRecord(false).build())) {
            printer.printRecord("SQLCaseId", "DatabaseType", "Result", "SQL");
            printer.flush();
        } catch (final IOException ex) {
            throw new RuntimeException("Create CSV file header failed.", ex);
        }
    }
    
    @Override
    public void printResult(final String sqlCaseId, final String databaseType, final boolean isSuccess, final String sql) {
        try {
            printer.printRecord(sqlCaseId, databaseType, isSuccess ? "success" : "failed", sql);
            printer.flush();
        } catch (final IOException ex) {
            throw new RuntimeException("Write CSV file failed.", ex);
        }
    }
}
