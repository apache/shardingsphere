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

package org.apache.shardingsphere.sql.parser.engine.opengauss;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenGaussSQLParserEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final ShardingSphereSQLParserEngine parserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Test
    void assertParseCreateIndexSQLWithoutMetadata() {
        String sql = "CREATE INDEX idx_user_id ON test.t_order USING btree (user_id) TABLESPACE pg_default";
        SQLStatement sqlStatement = parserEngine.parse(sql, false);
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(null, sqlStatement, "logic_db");
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        assertFalse(tablesContext.getSchemaName().isPresent());
    }
    
    @Test
    void assertParseCreateIndexSQLWithMetadata() {
        String sql = "CREATE INDEX idx_user_id ON test.t_order USING btree (user_id) TABLESPACE pg_default";
        SQLStatement sqlStatement = parserEngine.parse(sql, false);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), "logic_db", new HintValueContext()).bind(sqlStatement);
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        assertThat(tablesContext.getTableNames().iterator().next(), is("t_order"));
        assertThat(tablesContext.getSchemaName().isPresent(), is(true));
        assertThat(tablesContext.getSchemaName().get(), is("test"));
        assertThat(tablesContext.getDatabaseName().isPresent(), is(true));
        assertThat(tablesContext.getDatabaseName().get(), is("logic_db"));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", 0, false, false, false, true, false, false);
        ShardingSphereTable orderTable = new ShardingSphereTable("t_order", Collections.singletonList(userIdColumn), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema testSchema = new ShardingSphereSchema("test", Collections.singleton(orderTable), Collections.emptyList());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("logic_db");
        when(database.containsSchema("test")).thenReturn(true);
        when(database.getSchema("test")).thenReturn(testSchema);
        return new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
    }
}
