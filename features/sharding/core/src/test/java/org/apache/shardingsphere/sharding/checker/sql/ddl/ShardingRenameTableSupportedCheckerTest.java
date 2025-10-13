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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingRenameTableSupportedCheckerTest {
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertCheckWhenNewTableNameConflictsWithShardingTable() {
        SQLStatementContext sqlStatementContext = createRenameTableStatementContext("t_order", "t_user");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(rule.isShardingTable("t_user")).thenReturn(true);
        when(schema.containsTable("t_user")).thenReturn(false);
        assertThrows(UnsupportedShardingOperationException.class, () -> new ShardingRenameTableSupportedChecker().check(rule, mock(), schema, sqlStatementContext));
    }
    
    @Test
    void assertCheckWhenNewTableNameConflictsWithExistingTable() {
        SQLStatementContext sqlStatementContext = createRenameTableStatementContext("t_order", "t_existing");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(rule.isShardingTable("t_existing")).thenReturn(false);
        when(schema.containsTable("t_existing")).thenReturn(true);
        assertThrows(UnsupportedShardingOperationException.class, () -> new ShardingRenameTableSupportedChecker().check(rule, mock(), schema, sqlStatementContext));
    }
    
    @Test
    void assertCheckSuccess() {
        SQLStatementContext sqlStatementContext = createRenameTableStatementContext("t_order", "t_user_order");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(rule.isShardingTable("t_user_order")).thenReturn(false);
        when(schema.containsTable("t_user_order")).thenReturn(false);
        assertDoesNotThrow(() -> new ShardingRenameTableSupportedChecker().check(rule, mock(), schema, sqlStatementContext));
    }
    
    @Test
    void assertCheckWithMultipleTables() {
        RenameTableStatement sqlStatement = mock(RenameTableStatement.class);
        RenameTableDefinitionSegment segment1 = createRenameTableDefinitionSegment("t_order", "t_order_new");
        RenameTableDefinitionSegment segment2 = createRenameTableDefinitionSegment("t_user", "t_user_new");
        when(sqlStatement.getRenameTables()).thenReturn(Arrays.asList(segment1, segment2));
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(new TableSQLStatementAttribute(Arrays.asList(
                segment1.getTable(), segment1.getRenameTable(), segment2.getTable(), segment2.getRenameTable()))));
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(rule.isShardingTable("t_order_new")).thenReturn(false);
        when(rule.isShardingTable("t_user_new")).thenReturn(false);
        when(schema.containsTable("t_order_new")).thenReturn(false);
        when(schema.containsTable("t_user_new")).thenReturn(false);
        assertDoesNotThrow(() -> new ShardingRenameTableSupportedChecker().check(rule, mock(), schema, sqlStatementContext));
    }
    
    private RenameTableDefinitionSegment createRenameTableDefinitionSegment(final String originTableName, final String newTableName) {
        RenameTableDefinitionSegment renameTableDefinitionSegment = new RenameTableDefinitionSegment(0, 0);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(originTableName)));
        SimpleTableSegment renameTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(newTableName)));
        renameTableDefinitionSegment.setTable(table);
        renameTableDefinitionSegment.setRenameTable(renameTable);
        return renameTableDefinitionSegment;
    }
    
    private SQLStatementContext createRenameTableStatementContext(final String originTableName, final String newTableName) {
        RenameTableStatement sqlStatement = mock(RenameTableStatement.class);
        RenameTableDefinitionSegment renameTableDefinitionSegment = new RenameTableDefinitionSegment(0, 0);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(originTableName)));
        SimpleTableSegment renameTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(newTableName)));
        renameTableDefinitionSegment.setTable(table);
        renameTableDefinitionSegment.setRenameTable(renameTable);
        when(sqlStatement.getRenameTables()).thenReturn(Collections.singleton(renameTableDefinitionSegment));
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(new TableSQLStatementAttribute(Arrays.asList(table, renameTable))));
        return new CommonSQLStatementContext(sqlStatement);
    }
}
