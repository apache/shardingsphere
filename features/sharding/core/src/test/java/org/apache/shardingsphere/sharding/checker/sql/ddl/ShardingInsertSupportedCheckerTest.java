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
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.checker.sql.dml.ShardingInsertSupportedChecker;
import org.apache.shardingsphere.sharding.exception.syntax.DMLWithMultipleShardingTablesException;
import org.apache.shardingsphere.sharding.exception.syntax.InsertSelectTableViolationException;
import org.apache.shardingsphere.sharding.exception.syntax.MissingGenerateKeyColumnWithInsertSelectException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingInsertSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Mock
    private ShardingRule rule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckWhenInsertMultiTables() {
        InsertStatementContext sqlStatementContext = createInsertStatementContext(createInsertStatement());
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        when(rule.containsShardingTable(tableNames)).thenReturn(true);
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> new ShardingInsertSupportedChecker().check(rule, database, mock(), sqlStatementContext));
    }
    
    private InsertStatementContext createInsertStatementContext(final InsertStatement insertStatement) {
        when(database.getName()).thenReturn("foo_db");
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock());
        return new InsertStatementContext(insertStatement, metaData, "foo_db");
    }
    
    @Test
    void assertCheckWhenInsertSelectWithoutKeyGenerateColumn() {
        when(rule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(rule.isGenerateKeyColumn("id", "user")).thenReturn(false);
        InsertStatementContext sqlStatementContext = createInsertStatementContext(createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTableNames().addAll(createSingleTablesContext().getTableNames());
        assertThrows(MissingGenerateKeyColumnWithInsertSelectException.class, () -> new ShardingInsertSupportedChecker().check(rule, database, mock(), sqlStatementContext));
    }
    
    @Test
    void assertCheckWhenInsertSelectWithKeyGenerateColumn() {
        when(rule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(rule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        InsertStatementContext sqlStatementContext = createInsertStatementContext(createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTableNames().addAll(createSingleTablesContext().getTableNames());
        assertDoesNotThrow(() -> new ShardingInsertSupportedChecker().check(rule, database, mock(), sqlStatementContext));
    }
    
    @Test
    void assertCheckWhenInsertSelectWithoutBindingTables() {
        when(rule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(rule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        when(rule.containsShardingTable(multiTablesContext.getTableNames())).thenReturn(true);
        InsertStatementContext sqlStatementContext = createInsertStatementContext(createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTableNames().addAll(multiTablesContext.getTableNames());
        assertThrows(InsertSelectTableViolationException.class, () -> new ShardingInsertSupportedChecker().check(rule, database, mock(), sqlStatementContext));
    }
    
    @Test
    void assertCheckWhenInsertSelectWithBindingTables() {
        when(rule.findGenerateKeyColumnName("user")).thenReturn(Optional.of("id"));
        when(rule.isGenerateKeyColumn("id", "user")).thenReturn(true);
        TablesContext multiTablesContext = createMultiTablesContext();
        InsertStatementContext sqlStatementContext = createInsertStatementContext(createInsertSelectStatement());
        sqlStatementContext.getTablesContext().getTableNames().addAll(multiTablesContext.getTableNames());
        assertDoesNotThrow(() -> new ShardingInsertSupportedChecker().check(rule, database, mock(), sqlStatementContext));
    }
    
    private InsertStatement createInsertStatement() {
        InsertStatement result = mock(InsertStatement.class);
        when(result.getDatabaseType()).thenReturn(databaseType);
        when(result.getTable()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user")))));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("id"));
        List<ColumnSegment> columnSegments = Collections.singletonList(columnSegment);
        ColumnAssignmentSegment assignmentSegment = new ColumnAssignmentSegment(0, 0, columnSegments, new ParameterMarkerExpressionSegment(0, 0, 0));
        when(result.getOnDuplicateKeyColumns()).thenReturn(Optional.of(new OnDuplicateKeyColumnsSegment(0, 0, Collections.singletonList(assignmentSegment))));
        when(result.getColumns()).thenReturn(Collections.singleton(columnSegment));
        return result;
    }
    
    private InsertStatement createInsertSelectStatement() {
        InsertStatement result = createInsertStatement();
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        when(result.getInsertSelect()).thenReturn(Optional.of(new SubquerySegment(0, 0, selectStatement, "")));
        return result;
    }
    
    private TablesContext createSingleTablesContext() {
        List<SimpleTableSegment> result = new LinkedList<>();
        result.add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        return new TablesContext(result);
    }
    
    private TablesContext createMultiTablesContext() {
        List<SimpleTableSegment> result = new LinkedList<>();
        result.add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        result.add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order"))));
        return new TablesContext(result);
    }
}
