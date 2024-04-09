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

import org.apache.shardingsphere.infra.binder.context.statement.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sharding.exception.metadata.DuplicatedIndexException;
import org.apache.shardingsphere.sharding.exception.metadata.IndexNotExistedException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterIndexStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingAlterIndexStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertPreValidateAlterIndexWhenIndexExistRenameIndexNotExistForPostgreSQL() {
        PostgreSQLAlterIndexStatement sqlStatement = new PostgreSQLAlterIndexStatement();
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.setRenameIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(table.containsIndex("t_order_index")).thenReturn(true);
        when(table.containsIndex("t_order_index_new")).thenReturn(false);
        assertDoesNotThrow(() -> new ShardingAlterIndexStatementValidator().preValidate(
                shardingRule, new AlterIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateAlterIndexWhenIndexNotExistRenameIndexNotExistForPostgreSQL() {
        PostgreSQLAlterIndexStatement sqlStatement = new PostgreSQLAlterIndexStatement();
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.setRenameIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(table.containsIndex("t_order_index")).thenReturn(false);
        assertThrows(IndexNotExistedException.class,
                () -> new ShardingAlterIndexStatementValidator().preValidate(
                        shardingRule, new AlterIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateAlterIndexWhenIndexExistRenameIndexExistForPostgreSQL() {
        PostgreSQLAlterIndexStatement sqlStatement = new PostgreSQLAlterIndexStatement();
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.setRenameIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(table.containsIndex("t_order_index")).thenReturn(true);
        when(table.containsIndex("t_order_index_new")).thenReturn(true);
        assertThrows(DuplicatedIndexException.class,
                () -> new ShardingAlterIndexStatementValidator().preValidate(
                        shardingRule, new AlterIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
}
