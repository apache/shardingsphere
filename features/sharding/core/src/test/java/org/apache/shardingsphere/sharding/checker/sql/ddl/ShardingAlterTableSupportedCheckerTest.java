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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingAlterTableSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule rule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckWithRenameTableWhenNewTableNameConflictsWithShardingTable() {
        AlterTableStatement sqlStatement = new AlterTableStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user"))));
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(rule.isShardingTable("t_user")).thenReturn(true);
        when(schema.containsTable("t_user")).thenReturn(false);
        assertThrows(UnsupportedShardingOperationException.class, () -> new ShardingAlterTableSupportedChecker().check(rule, database, schema, sqlStatementContext));
    }
    
    @Test
    void assertCheckWithRenameTableWhenNewTableNameConflictsWithExistingTable() {
        AlterTableStatement sqlStatement = new AlterTableStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_existing"))));
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(rule.isShardingTable("t_existing")).thenReturn(false);
        when(schema.containsTable("t_existing")).thenReturn(true);
        assertThrows(UnsupportedShardingOperationException.class, () -> new ShardingAlterTableSupportedChecker().check(rule, database, schema, sqlStatementContext));
    }
    
    @Test
    void assertCheckWithRenameTableSuccess() {
        AlterTableStatement sqlStatement = new AlterTableStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_new"))));
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(rule.isShardingTable("t_order_new")).thenReturn(false);
        when(schema.containsTable("t_order_new")).thenReturn(false);
        assertDoesNotThrow(() -> new ShardingAlterTableSupportedChecker().check(rule, database, schema, sqlStatementContext));
    }
    
    @Test
    void assertCheckWithoutRenameTable() {
        AlterTableStatement sqlStatement = new AlterTableStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        assertDoesNotThrow(() -> new ShardingAlterTableSupportedChecker().check(rule, database, mock(ShardingSphereSchema.class), sqlStatementContext));
    }
}
