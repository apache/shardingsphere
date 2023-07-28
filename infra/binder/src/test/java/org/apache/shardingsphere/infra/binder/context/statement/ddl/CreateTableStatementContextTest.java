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

package org.apache.shardingsphere.infra.binder.context.statement.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.ConstraintAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTableStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateTableStatementContextTest {
    
    @Test
    void assertMySQLNewInstance() {
        assertNewInstance(mock(MySQLCreateTableStatement.class));
    }
    
    @Test
    void assertPostgreSQLNewInstance() {
        assertNewInstance(mock(PostgreSQLCreateTableStatement.class));
    }
    
    @Test
    void assertOracleNewInstance() {
        assertNewInstance(mock(OracleCreateTableStatement.class));
    }
    
    @Test
    void assertSQLServerNewInstance() {
        assertNewInstance(mock(SQLServerCreateTableStatement.class));
    }
    
    @Test
    void assertSQL92NewInstance() {
        assertNewInstance(mock(SQL92CreateTableStatement.class));
    }
    
    private void assertNewInstance(final CreateTableStatement createTableStatement) {
        CreateTableStatementContext actual = new CreateTableStatementContext(createTableStatement);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        when(createTableStatement.getTable()).thenReturn(table);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual, instanceOf(ConstraintAvailable.class));
        ColumnDefinitionSegment columnDefinition = mock(ColumnDefinitionSegment.class);
        when(columnDefinition.getReferencedTables()).thenReturn(Collections.singletonList(table));
        when(createTableStatement.getColumnDefinitions()).thenReturn(Collections.singletonList(columnDefinition));
        ConstraintDefinitionSegment constraintDefinition = mock(ConstraintDefinitionSegment.class);
        when(constraintDefinition.getConstraintName()).thenReturn(Optional.of(new ConstraintSegment(0, 0, new IdentifierValue("fk_1"))));
        when(constraintDefinition.getReferencedTable()).thenReturn(Optional.of(table));
        when(createTableStatement.getConstraintDefinitions()).thenReturn(Collections.singletonList(constraintDefinition));
        assertThat(actual.getSqlStatement(), is(createTableStatement));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_1", "tbl_1")));
        when(constraintDefinition.getIndexName()).thenReturn(Optional.of(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("index_1")))));
        assertThat(actual.getIndexes().stream().map(each -> each.getIndexName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList("index_1")));
        assertThat(actual.getConstraints().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList("fk_1")));
    }
}
