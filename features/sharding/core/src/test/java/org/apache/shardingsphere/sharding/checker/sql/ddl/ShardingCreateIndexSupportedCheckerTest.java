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
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.DuplicateIndexException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingCreateIndexSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule rule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckWhenTableExistIndexNotExistForPostgreSQL() {
        CreateIndexStatement sqlStatement = new CreateIndexStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.buildAttributes();
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        assertDoesNotThrow(() -> new ShardingCreateIndexSupportedChecker().check(rule, database, schema, new CommonSQLStatementContext(sqlStatement)));
    }
    
    @Test
    void assertCheckWhenTableNotExistIndexNotExistForPostgreSQL() {
        CreateIndexStatement sqlStatement = new CreateIndexStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.buildAttributes();
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(false);
        assertThrows(NoSuchTableException.class, () -> new ShardingCreateIndexSupportedChecker().check(rule, database, schema, new CommonSQLStatementContext(sqlStatement)));
    }
    
    @Test
    void assertCheckWhenTableExistIndexExistForPostgreSQL() {
        CreateIndexStatement sqlStatement = new CreateIndexStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.buildAttributes();
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(schema.containsIndex("t_order", "t_order_index")).thenReturn(true);
        assertThrows(DuplicateIndexException.class, () -> new ShardingCreateIndexSupportedChecker().check(rule, database, schema, new CommonSQLStatementContext(sqlStatement)));
    }
    
    @Test
    void assertCheckWithoutIndexNameWhenTableExistIndexNotExistForPostgreSQL() {
        CreateIndexStatement sqlStatement = new CreateIndexStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("content")));
        sqlStatement.setAnonymousIndexStartIndex(10);
        sqlStatement.buildAttributes();
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        assertDoesNotThrow(() -> new ShardingCreateIndexSupportedChecker().check(rule, database, schema, new CommonSQLStatementContext(sqlStatement)));
    }
    
    @Test
    void assertCheckWithoutIndexNameWhenTableNotExistIndexNotExistForPostgreSQL() {
        CreateIndexStatement sqlStatement = new CreateIndexStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("content")));
        sqlStatement.setAnonymousIndexStartIndex(10);
        sqlStatement.buildAttributes();
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(false);
        assertThrows(NoSuchTableException.class, () -> new ShardingCreateIndexSupportedChecker().check(rule, database, schema, new CommonSQLStatementContext(sqlStatement)));
    }
    
    @Test
    void assertCheckWithoutIndexNameWhenTableExistIndexExistForPostgreSQL() {
        CreateIndexStatement sqlStatement = new CreateIndexStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("content")));
        sqlStatement.setAnonymousIndexStartIndex(10);
        sqlStatement.buildAttributes();
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(schema.containsIndex("t_order", "content_idx")).thenReturn(true);
        assertThrows(DuplicateIndexException.class, () -> new ShardingCreateIndexSupportedChecker().check(rule, database, schema, new CommonSQLStatementContext(sqlStatement)));
    }
}
