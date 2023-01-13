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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowLogicalTablesStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule.LogicalTableResultSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class LogicalTableResultSetTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("sharding_db");
        when(database.getProtocolType()).thenReturn(mock(DatabaseType.class));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema("sharding_db")).thenReturn(schema);
        when(schema.getAllTableNames()).thenReturn(Arrays.asList("t_order", "t_order_item"));
    }
    
    @Test
    public void assertGetRowData() {
        LogicalTableResultSet resultSet = new LogicalTableResultSet();
        resultSet.init(database, mock(ShowLogicalTablesStatement.class));
        assertThat(resultSet.getRowData().iterator().next(), is("t_order"));
        assertThat(resultSet.getRowData().iterator().next(), is("t_order_item"));
    }
    
    @Test
    public void assertRowDataWithLike() {
        LogicalTableResultSet resultSet = new LogicalTableResultSet();
        resultSet.init(database, new ShowLogicalTablesStatement("t_order_%", null));
        assertThat(resultSet.getRowData().iterator().next(), is("t_order_item"));
    }
}
