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

package org.apache.shardingsphere.sharding.checker.sql.dml;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.exception.syntax.DMLWithMultipleShardingTablesException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingMultipleTablesSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertCheckWhenUpdateSingleTable() {
        UpdateStatement updateStatement = createUpdateStatement();
        updateStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        UpdateStatementContext sqlStatementContext = new UpdateStatementContext(updateStatement);
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        when(rule.isAllShardingTables(tableNames)).thenReturn(true);
        when(rule.containsShardingTable(tableNames)).thenReturn(true);
        assertDoesNotThrow(() -> new ShardingMultipleTablesSupportedChecker().check(rule, mock(), mock(), sqlStatementContext));
    }
    
    @Test
    void assertCheckWhenUpdateMultipleTables() {
        UpdateStatement updateStatement = createUpdateStatement();
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        joinTableSegment.setRight(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order"))));
        updateStatement.setTable(joinTableSegment);
        UpdateStatementContext sqlStatementContext = new UpdateStatementContext(updateStatement);
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        when(rule.containsShardingTable(tableNames)).thenReturn(true);
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> new ShardingMultipleTablesSupportedChecker().check(rule, mock(), mock(), sqlStatementContext));
    }
    
    private UpdateStatement createUpdateStatement() {
        UpdateStatement result = new UpdateStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(0, 0, new IdentifierValue("id")));
        ColumnAssignmentSegment assignment = new ColumnAssignmentSegment(0, 0, columns, new LiteralExpressionSegment(0, 0, 1));
        result.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singleton(assignment)));
        return result;
    }
}
