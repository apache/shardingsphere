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

package org.apache.shardingsphere.infra.parser.sql;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class SQLStatementParserEngineFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "SQL92");
    
    @Test
    void assertGetSQLStatementParserEngineNotSame() {
        SQLStatementParserEngine before = SQLStatementParserEngineFactory.getSQLStatementParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(64, 1024L));
        SQLStatementParserEngine after = SQLStatementParserEngineFactory.getSQLStatementParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
        assertSame(before, after);
    }
    
    @Test
    void assertGetSQLStatementParserEngineSame() {
        SQLStatementParserEngine before = SQLStatementParserEngineFactory.getSQLStatementParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
        SQLStatementParserEngine after = SQLStatementParserEngineFactory.getSQLStatementParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
        assertSame(before, after);
    }
}
