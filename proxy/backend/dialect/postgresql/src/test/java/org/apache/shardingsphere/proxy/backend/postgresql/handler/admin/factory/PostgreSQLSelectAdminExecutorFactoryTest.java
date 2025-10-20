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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.factory;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PostgreSQLSelectAdminExecutorFactoryTest {
    
    @Test
    void assertNewInstanceWithPgDatabaseSystemTable() {
        String sql = "SELECT d.datname as \"Name\",pg_catalog.pg_get_userbyid(d.datdba) as \"Owner\",pg_catalog.pg_encoding_to_char(d.encoding) as \"Encoding\","
                + "d.datcollate as \"Collate\",d.datctype as \"Ctype\",pg_catalog.array_to_string(d.datacl, E'\\n') AS \"Access privileges\" FROM pg_catalog.pg_database d ORDER BY 1";
        SelectStatementContext selectStatementContext = new SelectStatementContext(parseSQL(sql), mock(), null, Collections.emptyList());
        assertTrue(PostgreSQLSelectAdminExecutorFactory.newInstance(selectStatementContext, sql, Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertNewInstanceWithShardingSphereSystemTable() {
        String sql = "SELECT * FROM shardingsphere.cluster_information";
        SelectStatementContext selectStatementContext = new SelectStatementContext(parseSQL(sql), mock(), null, Collections.emptyList());
        assertFalse(PostgreSQLSelectAdminExecutorFactory.newInstance(selectStatementContext, sql, Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertNewInstanceWithSelectLogicSQL() {
        String sql = "SELECT * FROM foo_tbl";
        SelectStatementContext selectStatementContext = new SelectStatementContext(parseSQL(sql), mock(), null, Collections.emptyList());
        assertFalse(PostgreSQLSelectAdminExecutorFactory.newInstance(selectStatementContext, sql, Collections.emptyList()).isPresent());
    }
    
    private SelectStatement parseSQL(final String sql) {
        CacheOption cacheOption = new CacheOption(0, 0L);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        return (SelectStatement) new SQLParserRule(new SQLParserRuleConfiguration(cacheOption, cacheOption)).getSQLParserEngine(databaseType).parse(sql, false);
    }
}
