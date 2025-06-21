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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.ValidateConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.DropIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterTableStatementContextTest {
    
    @Test
    void assertNewInstance() {
        AlterTableStatement alterTableStatement = new AlterTableStatement();
        SimpleTableSegment table = new SimpleTableSegment(createTableNameSegment("foo_tbl"));
        alterTableStatement.setTable(table);
        alterTableStatement.setRenameTable(new SimpleTableSegment(createTableNameSegment("rename_foo_tbl")));
        Collection<SimpleTableSegment> referencedTables = Collections.singletonList(table);
        ColumnDefinitionSegment columnDefinition = mock(ColumnDefinitionSegment.class);
        when(columnDefinition.getReferencedTables()).thenReturn(referencedTables);
        AddColumnDefinitionSegment addColumnDefinition = mock(AddColumnDefinitionSegment.class);
        when(addColumnDefinition.getColumnDefinitions()).thenReturn(Collections.singletonList(columnDefinition));
        alterTableStatement.getAddColumnDefinitions().add(addColumnDefinition);
        ModifyColumnDefinitionSegment modifyColumnDefinition = mock(ModifyColumnDefinitionSegment.class);
        when(modifyColumnDefinition.getColumnDefinition()).thenReturn(columnDefinition);
        alterTableStatement.getModifyColumnDefinitions().add(modifyColumnDefinition);
        ConstraintDefinitionSegment constraintDefinition = mock(ConstraintDefinitionSegment.class);
        when(constraintDefinition.getReferencedTable()).thenReturn(Optional.of(table));
        when(constraintDefinition.getIndexName()).thenReturn(Optional.of(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("index")))));
        AddConstraintDefinitionSegment addConstraintDefinition = mock(AddConstraintDefinitionSegment.class);
        when(addConstraintDefinition.getConstraintDefinition()).thenReturn(constraintDefinition);
        ConstraintSegment constraint = new ConstraintSegment(0, 0, new IdentifierValue("constraint"));
        when(addConstraintDefinition.getConstraintDefinition().getConstraintName()).thenReturn(Optional.of(constraint));
        alterTableStatement.getAddConstraintDefinitions().add(addConstraintDefinition);
        ValidateConstraintDefinitionSegment validateConstraintDefinition = mock(ValidateConstraintDefinitionSegment.class);
        when(validateConstraintDefinition.getConstraintName()).thenReturn(constraint);
        alterTableStatement.getValidateConstraintDefinitions().add(validateConstraintDefinition);
        DropConstraintDefinitionSegment dropConstraintDefinition = mock(DropConstraintDefinitionSegment.class);
        when(dropConstraintDefinition.getConstraintName()).thenReturn(constraint);
        alterTableStatement.getDropConstraintDefinitions().add(dropConstraintDefinition);
        DropIndexDefinitionSegment dropIndexDefinitionSegment = mock(DropIndexDefinitionSegment.class);
        when(dropIndexDefinitionSegment.getIndexSegment()).thenReturn(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("drop_index"))));
        alterTableStatement.getDropIndexDefinitions().add(dropIndexDefinitionSegment);
        AlterTableStatementContext actual = new AlterTableStatementContext(mock(), alterTableStatement);
        assertThat(actual.getSqlStatement(), is(alterTableStatement));
        assertThat(actual.getTablesContext().getSimpleTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("foo_tbl", "rename_foo_tbl", "foo_tbl", "foo_tbl", "foo_tbl")));
        assertThat(actual.getIndexes().stream().map(each -> each.getIndexName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("index", "drop_index")));
    }
    
    private TableNameSegment createTableNameSegment(final String tableName) {
        TableNameSegment result = new TableNameSegment(0, 0, new IdentifierValue(tableName));
        result.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        return result;
    }
}
