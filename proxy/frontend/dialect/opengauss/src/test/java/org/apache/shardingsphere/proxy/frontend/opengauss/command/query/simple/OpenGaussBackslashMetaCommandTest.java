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

package org.apache.shardingsphere.proxy.frontend.opengauss.command.query.simple;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OpenGaussBackslashMetaCommandTest {
    
    @Test
    @DisplayName("assertParseListDatabasesSQLWithOpenGaussDialect")
    void assertParseListDatabasesSQLWithOpenGaussDialect() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        ShardingSphereSQLParserEngine parserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(128, 64), new CacheOption(128, 64));
        String sql = "SELECT d.datname AS \"Name\",\n"
                + "       pg_catalog.pg_get_userbyid(d.datdba) AS \"Owner\",\n"
                + "       pg_catalog.pg_encoding_to_char(d.encoding) AS \"Encoding\",\n"
                + "       d.datcollate AS \"Collate\",\n"
                + "       d.datctype AS \"Ctype\",\n"
                + "       pg_catalog.array_to_string(d.datacl, E'\\\\n') AS \"Access privileges\"\n"
                + "FROM pg_catalog.pg_database d\n"
                + "ORDER BY 1;";
        assertDoesNotThrow(() -> parserEngine.parse(sql, false));
    }
}
