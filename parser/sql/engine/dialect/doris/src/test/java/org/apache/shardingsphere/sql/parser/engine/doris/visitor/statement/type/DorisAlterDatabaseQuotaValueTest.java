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

package org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DorisAlterDatabaseQuotaValueTest {
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"ALTER DATABASE db SET DATA QUOTA 99999999999P", "ALTER DATABASE db SET DATA QUOTA 99999999999999999999999",
            "ALTER DATABASE db SET DATA QUOTA foo", "ALTER DATABASE db SET DATA QUOTA 1XB", "ALTER DATABASE db SET DATA QUOTA 1.5",
            "ALTER DATABASE db SET REPLICA QUOTA 1.5", "ALTER DATABASE db SET TRANSACTION QUOTA 2.75"})
    void assertInvalidQuotaValueRejected(final String sql) {
        CacheOption cacheOption = new CacheOption(128, 1024L);
        SQLParserEngine parserEngine = new SQLParserEngine("Doris", cacheOption);
        SQLStatementVisitorEngine visitorEngine = new SQLStatementVisitorEngine("Doris");
        assertThrows(SQLParsingException.class, () -> visitorEngine.visit(parserEngine.parse(sql, false)));
    }
}
