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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.sharding.exception.DuplicatedIndexException;
import org.apache.shardingsphere.sharding.exception.IndexNotExistedException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterIndexStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
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
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    public void assertPreValidateAlterIndexWhenIndexExistRenameIndexNotExistForPostgreSQL() {
        PostgreSQLAlterIndexStatement sqlStatement = new PostgreSQLAlterIndexStatement();
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.setRenameIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        Map<String, ShardingSphereIndex> indexes = mock(HashMap.class);
        when(table.getIndexes()).thenReturn(indexes);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(indexes.containsKey("t_order_index")).thenReturn(true);
        when(indexes.containsKey("t_order_index_new")).thenReturn(false);
        new ShardingAlterIndexStatementValidator().preValidate(shardingRule, new AlterIndexStatementContext(sqlStatement), Collections.emptyList(), database);
    }
    
    @Test(expected = IndexNotExistedException.class)
    public void assertPreValidateAlterIndexWhenIndexNotExistRenameIndexNotExistForPostgreSQL() {
        PostgreSQLAlterIndexStatement sqlStatement = new PostgreSQLAlterIndexStatement();
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.setRenameIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        Map<String, ShardingSphereIndex> indexes = mock(HashMap.class);
        when(table.getIndexes()).thenReturn(indexes);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(indexes.containsKey("t_order_index")).thenReturn(false);
        new ShardingAlterIndexStatementValidator().preValidate(shardingRule, new AlterIndexStatementContext(sqlStatement), Collections.emptyList(), database);
    }
    
    @Test(expected = DuplicatedIndexException.class)
    public void assertPreValidateAlterIndexWhenIndexExistRenameIndexExistForPostgreSQL() {
        PostgreSQLAlterIndexStatement sqlStatement = new PostgreSQLAlterIndexStatement();
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.setRenameIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        Map<String, ShardingSphereIndex> indexes = mock(HashMap.class);
        when(table.getIndexes()).thenReturn(indexes);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(indexes.containsKey("t_order_index")).thenReturn(true);
        when(indexes.containsKey("t_order_index_new")).thenReturn(true);
        new ShardingAlterIndexStatementValidator().preValidate(shardingRule, new AlterIndexStatementContext(sqlStatement), Collections.emptyList(), database);
    }
}
