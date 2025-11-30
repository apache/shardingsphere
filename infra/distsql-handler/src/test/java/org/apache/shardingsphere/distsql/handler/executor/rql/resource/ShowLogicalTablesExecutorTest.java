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

package org.apache.shardingsphere.distsql.handler.executor.rql.resource;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowLogicalTablesStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShowLogicalTablesExecutorTest {
    
    private final ShowLogicalTablesExecutor executor = new ShowLogicalTablesExecutor();
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema("foo_db")).thenReturn(schema);
        Collection<ShardingSphereTable> tables = Arrays.asList(mockShardingSphereTable("t_order"), mockShardingSphereTable("t_order_item"));
        when(schema.getAllTables()).thenReturn(tables);
        executor.setDatabase(database);
    }
    
    @Test
    void assertGetRowData() {
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowLogicalTablesStatement.class), mock(ContextManager.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order"));
        row = iterator.next();
        assertThat(row.getCell(1), is("t_order_item"));
    }
    
    @Test
    void assertRowDataWithLike() {
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowLogicalTablesStatement(false, null, "t_order_%"), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        assertThat(iterator.next().getCell(1), is("t_order_item"));
    }
    
    @Test
    void assertRowDataWithFullAndLike() {
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowLogicalTablesStatement(true, null, "t_order_%"), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("t_order_item"));
        assertThat(row.getCell(2), is("TABLE"));
    }
    
    private ShardingSphereTable mockShardingSphereTable(final String tableName) {
        ShardingSphereTable result = mock(ShardingSphereTable.class);
        when(result.getName()).thenReturn(tableName);
        when(result.getType()).thenReturn(TableType.TABLE);
        return result;
    }
}
