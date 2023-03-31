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

package org.apache.shardingsphere.test.it.sql.parser.external.result.type.log;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.it.sql.parser.external.result.SQLParseResultReporter;

/**
 * SQL parse result reporter for log.
 */
@Slf4j
public final class LogSQLParseResultReporter implements SQLParseResultReporter {
    
    @Override
    public void printResult(final String sqlCaseId, final String databaseType, final boolean isSuccess, final String sql) {
        if (!isSuccess) {
            log.warn("SQL parse failed. SQL Case ID is: {}, database type is: {}, SQL is: {}", sqlCaseId, databaseType, sql);
        }
    }
    
    @Override
    public void close() {
    }
}
