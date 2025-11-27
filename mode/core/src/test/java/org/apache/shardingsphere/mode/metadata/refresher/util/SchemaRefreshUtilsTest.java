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

package org.apache.shardingsphere.mode.metadata.refresher.util;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchemaRefreshUtilsTest {
    
    @Test
    void assertGetSchemaNameWithSchemaFromContext() {
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSchemaName()).thenReturn(Optional.of("Foo_Schema"));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        assertThat(SchemaRefreshUtils.getSchemaName(mock(), sqlStatementContext), is("foo_schema"));
    }
    
    @Test
    void assertGetSchemaNameWithDefaultSchema() {
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSchemaName()).thenReturn(Optional.empty());
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(sqlStatementContext.getSqlStatement()).thenReturn(new SQLStatement(TypedSPILoader.getService(DatabaseType.class, "FIXTURE")));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("FOO_DB");
        assertThat(SchemaRefreshUtils.getSchemaName(database, sqlStatementContext), is("foo_db"));
    }
}
