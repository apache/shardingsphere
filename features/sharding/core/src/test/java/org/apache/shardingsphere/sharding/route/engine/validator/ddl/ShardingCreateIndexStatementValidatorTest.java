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

import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sharding.exception.metadata.DuplicatedIndexException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateIndexStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateIndexStatement;
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
class ShardingCreateIndexStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertPreValidateCreateIndexWhenTableExistIndexNotExistForPostgreSQL() {
        PostgreSQLCreateIndexStatement sqlStatement = new PostgreSQLCreateIndexStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        when(database.getSchema("public").containsTable("t_order")).thenReturn(true);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        assertDoesNotThrow(() -> new ShardingCreateIndexStatementValidator().preValidate(
                shardingRule, new CreateIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateCreateIndexWhenTableNotExistIndexNotExistForPostgreSQL() {
        PostgreSQLCreateIndexStatement sqlStatement = new PostgreSQLCreateIndexStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        when(database.getSchema("public").containsTable("t_order")).thenReturn(false);
        assertThrows(NoSuchTableException.class,
                () -> new ShardingCreateIndexStatementValidator().preValidate(
                        shardingRule, new CreateIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateCreateIndexWhenTableExistIndexExistForPostgreSQL() {
        PostgreSQLCreateIndexStatement sqlStatement = new PostgreSQLCreateIndexStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        when(database.getSchema("public").containsTable("t_order")).thenReturn(true);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(database.getSchema("public").containsIndex("t_order", "t_order_index")).thenReturn(true);
        assertThrows(DuplicatedIndexException.class,
                () -> new ShardingCreateIndexStatementValidator().preValidate(
                        shardingRule, new CreateIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateCreateIndexWithoutIndexNameWhenTableExistIndexNotExistForPostgreSQL() {
        PostgreSQLCreateIndexStatement sqlStatement = new PostgreSQLCreateIndexStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("content")));
        sqlStatement.setGeneratedIndexStartIndex(10);
        when(database.getSchema("public").containsTable("t_order")).thenReturn(true);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        assertDoesNotThrow(() -> new ShardingCreateIndexStatementValidator().preValidate(
                shardingRule, new CreateIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateCreateIndexWithoutIndexNameWhenTableNotExistIndexNotExistForPostgreSQL() {
        PostgreSQLCreateIndexStatement sqlStatement = new PostgreSQLCreateIndexStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("content")));
        sqlStatement.setGeneratedIndexStartIndex(10);
        when(database.getSchema("public").containsTable("t_order")).thenReturn(false);
        assertThrows(NoSuchTableException.class,
                () -> new ShardingCreateIndexStatementValidator().preValidate(
                        shardingRule, new CreateIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateCreateIndexWithoutIndexNameWhenTableExistIndexExistForPostgreSQL() {
        PostgreSQLCreateIndexStatement sqlStatement = new PostgreSQLCreateIndexStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("content")));
        sqlStatement.setGeneratedIndexStartIndex(10);
        when(database.getSchema("public").containsTable("t_order")).thenReturn(true);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(database.getSchema("public").containsIndex("t_order", "content_idx")).thenReturn(true);
        assertThrows(DuplicatedIndexException.class,
                () -> new ShardingCreateIndexStatementValidator().preValidate(
                        shardingRule, new CreateIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
}
