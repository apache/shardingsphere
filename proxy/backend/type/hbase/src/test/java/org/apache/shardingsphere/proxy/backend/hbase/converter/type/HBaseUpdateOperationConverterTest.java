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

package org.apache.shardingsphere.proxy.backend.hbase.converter.type;

import org.apache.hadoop.hbase.client.Put;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverterFactory;
import org.apache.shardingsphere.proxy.backend.hbase.converter.operation.HBaseUpdateOperation;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HBaseUpdateOperationConverterTest {
    
    @Test
    void assertConvert() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getUpdateStatement());
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), DefaultDatabase.LOGIC_NAME).bind(sqlStatement, Collections.emptyList());
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        HBaseOperation operation = converter.convert();
        assertThat(operation.getTableName(), is(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
        assertThat(operation.getOperation(), instanceOf(Put.class));
    }
    
    @Test
    void assertConvertWithIn() {
        String sql = " update /*+ hbase */ t_test_order set age = 10 where rowKey in (1, '2')";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), DefaultDatabase.LOGIC_NAME).bind(sqlStatement, Collections.emptyList());
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        HBaseOperation operation = converter.convert();
        assertThat(operation.getTableName(), is(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
        assertThat(operation.getOperation(), instanceOf(HBaseUpdateOperation.class));
        assertThat(((HBaseUpdateOperation) operation.getOperation()).getPuts().size(), is(2));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereTable table = new ShardingSphereTable("t_test_order", Arrays.asList(new ShardingSphereColumn("rowKey", Types.VARCHAR, true, false, false, false, true, false),
                new ShardingSphereColumn("age", Types.INTEGER, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        when(database.getSchema(DefaultDatabase.LOGIC_NAME).getTable("t_test_order")).thenReturn(table);
        when(database.containsSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(true);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME).containsTable("t_test_order")).thenReturn(true);
        Map<String, ShardingSphereDatabase> databases = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database);
        return new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        
    }
}
