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

import org.apache.shardingsphere.infra.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sharding.route.engine.exception.NoSuchTableException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateIndexStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateIndexStatement;
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
public final class ShardingCreateIndexStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    public void assertPreValidateCreateIndexWhenTableExistIndexNotExistForPostgreSQL() {
        PostgreSQLCreateIndexStatement sqlStatement = new PostgreSQLCreateIndexStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        sqlStatement.setIndex(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        when(schema.containsTable("t_order")).thenReturn(true);
        TableMetaData tableMetaData = mock(TableMetaData.class);
        when(schema.get("t_order")).thenReturn(tableMetaData);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(indexes.containsKey("t_order_index")).thenReturn(false);
        new ShardingCreateIndexStatementValidator().preValidate(shardingRule, new CreateIndexStatementContext(sqlStatement), Collections.emptyList(), schema);
    }
    
    @Test(expected = NoSuchTableException.class)
    public void assertPreValidateCreateIndexWhenTableNotExistIndexNotExistForPostgreSQL() {
        PostgreSQLCreateIndexStatement sqlStatement = new PostgreSQLCreateIndexStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        sqlStatement.setIndex(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        when(schema.containsTable("t_order")).thenReturn(false);
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        new ShardingCreateIndexStatementValidator().preValidate(shardingRule, new CreateIndexStatementContext(sqlStatement), Collections.emptyList(), schema);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPreValidateCreateIndexWhenTableExistIndexExistForPostgreSQL() {
        PostgreSQLCreateIndexStatement sqlStatement = new PostgreSQLCreateIndexStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        sqlStatement.setIndex(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        when(schema.containsTable("t_order")).thenReturn(true);
        TableMetaData tableMetaData = mock(TableMetaData.class);
        when(schema.get("t_order")).thenReturn(tableMetaData);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(indexes.containsKey("t_order_index")).thenReturn(true);
        new ShardingCreateIndexStatementValidator().preValidate(shardingRule, new CreateIndexStatementContext(sqlStatement), Collections.emptyList(), schema);
    }
}
