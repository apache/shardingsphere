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

package org.apache.shardingsphere.sql.parser.result;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 *  CSV result generator.
 */
public final class CSVResultGenerator {
    
    private final CSVPrinter printer;
    
    public CSVResultGenerator(final String databaseType) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader("SQLCaseId", "DatabaseType", "Result", "SQL").setSkipHeaderRecord(false).build();
        try {
            Writer out = new FileWriter(databaseType + "-result.csv", true);
            printer = new CSVPrinter(out, csvFormat);
        } catch (final IOException ex) {
            throw new RuntimeException("Create CSV file failed.", ex);
        }
    }
    
    /**
     * Process the result.
     *
     * @param params the content for a row of CSV record
     */
    public void processResult(final Object... params) {
        try {
            printer.printRecord(params);
            // TODO this may be optimized in next step.
            printer.flush();
        } catch (final IOException ex) {
            throw new RuntimeException("Write CSV file failed.", ex);
        }
    }
}
