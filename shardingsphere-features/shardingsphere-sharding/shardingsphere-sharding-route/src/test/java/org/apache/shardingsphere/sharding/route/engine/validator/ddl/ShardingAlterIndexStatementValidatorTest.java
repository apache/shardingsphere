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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl;

import org.apache.shardingsphere.infra.binder.statement.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterIndexStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingAlterIndexStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    public void assertPreValidateAlterIndexWhenIndexExistRenameIndexNotExistForPostgreSQL() {
        PostgreSQLAlterIndexStatement sqlStatement = new PostgreSQLAlterIndexStatement();
        sqlStatement.setIndex(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        sqlStatement.setRenameIndex(new IndexSegment(0, 0, new IdentifierValue("t_order_index_new")));
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(schema.getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(schema.get("t_order")).thenReturn(tableMetaData);
        when(indexes.containsKey("t_order_index")).thenReturn(true);
        when(indexes.containsKey("t_order_index_new")).thenReturn(false);
        new ShardingAlterIndexStatementValidator().preValidate(shardingRule, new AlterIndexStatementContext(sqlStatement), Collections.emptyList(), schema);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPreValidateAlterIndexWhenIndexNotExistRenameIndexNotExistForPostgreSQL() {
        PostgreSQLAlterIndexStatement sqlStatement = new PostgreSQLAlterIndexStatement();
        sqlStatement.setIndex(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        sqlStatement.setRenameIndex(new IndexSegment(0, 0, new IdentifierValue("t_order_index_new")));
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(schema.getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(schema.get("t_order")).thenReturn(tableMetaData);
        when(indexes.containsKey("t_order_index")).thenReturn(false);
        new ShardingAlterIndexStatementValidator().preValidate(shardingRule, new AlterIndexStatementContext(sqlStatement), Collections.emptyList(), schema);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPreValidateAlterIndexWhenIndexExistRenameIndexExistForPostgreSQL() {
        PostgreSQLAlterIndexStatement sqlStatement = new PostgreSQLAlterIndexStatement();
        sqlStatement.setIndex(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        sqlStatement.setRenameIndex(new IndexSegment(0, 0, new IdentifierValue("t_order_index_new")));
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(schema.getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(schema.get("t_order")).thenReturn(tableMetaData);
        when(indexes.containsKey("t_order_index")).thenReturn(true);
        when(indexes.containsKey("t_order_index_new")).thenReturn(true);
        new ShardingAlterIndexStatementValidator().preValidate(shardingRule, new AlterIndexStatementContext(sqlStatement), Collections.emptyList(), schema);
    }
}
