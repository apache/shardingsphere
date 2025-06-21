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

package org.apache.shardingsphere.infra.binder.context.statement.type.ddl;

import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateTableStatementContextTest {
    
    @Test
    void assertNewInstance() {
        CreateTableStatement sqlStatement = new CreateTableStatement();
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")));
        sqlStatement.setTable(table);
        ColumnDefinitionSegment columnDefinition = mock(ColumnDefinitionSegment.class);
        when(columnDefinition.getReferencedTables()).thenReturn(Collections.singletonList(table));
        sqlStatement.getColumnDefinitions().add(columnDefinition);
        ConstraintDefinitionSegment constraintDefinition = mock(ConstraintDefinitionSegment.class);
        when(constraintDefinition.getConstraintName()).thenReturn(Optional.of(new ConstraintSegment(0, 0, new IdentifierValue("foo_fk"))));
        when(constraintDefinition.getReferencedTable()).thenReturn(Optional.of(table));
        sqlStatement.getConstraintDefinitions().add(constraintDefinition);
        CreateTableStatementContext actual = new CreateTableStatementContext(mock(), sqlStatement);
        assertThat(actual.getSqlStatement(), is(sqlStatement));
        when(constraintDefinition.getIndexName()).thenReturn(Optional.of(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("foo_idx")))));
        assertThat(actual.getIndexes().stream().map(each -> each.getIndexName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList("foo_idx")));
    }
}
