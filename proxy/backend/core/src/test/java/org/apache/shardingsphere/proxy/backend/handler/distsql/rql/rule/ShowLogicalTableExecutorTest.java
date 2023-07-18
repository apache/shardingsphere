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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowLogicalTablesStatement;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
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
class ShowLogicalTableExecutorTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void before() {
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(mock(DatabaseType.class));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema("foo_db")).thenReturn(schema);
        when(schema.getAllTableNames()).thenReturn(Arrays.asList("t_order", "t_order_item"));
    }
    
    @Test
    void assertGetRowData() {
        RQLExecutor<ShowLogicalTablesStatement> executor = new ShowLogicalTableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowLogicalTablesStatement.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order"));
        row = iterator.next();
        assertThat(row.getCell(1), is("t_order_item"));
    }
    
    @Test
    void assertRowDataWithLike() {
        RQLExecutor<ShowLogicalTablesStatement> executor = new ShowLogicalTableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, new ShowLogicalTablesStatement("t_order_%", null));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        assertThat(iterator.next().getCell(1), is("t_order_item"));
    }
    
    @Test
    void assertGetColumnNames() {
        RQLExecutor<ShowLogicalTablesStatement> executor = new ShowLogicalTableExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(1));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("table_name"));
    }
}
