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

package org.apache.shardingsphere.test.sql.parser.integrate.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.definition.ColumnDefinitionAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.definition.ColumnPositionAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.definition.ConstraintDefinitionAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.definition.ExpectedAddColumnDefinition;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.definition.ExpectedColumnDefinition;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.definition.ExpectedModifyColumnDefinition;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.ddl.AlterTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Alter table statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterTableStatementAssert {
    
    /**
     * Assert alter table statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual alter table statement
     * @param expected expected alter table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertAddColumnDefinitions(assertContext, actual, expected);
        assertAddConstraintDefinitions(assertContext, actual, expected);
        assertModifyColumnDefinitions(assertContext, actual, expected);
        assertDropColumns(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
    }
    
    private static void assertAddColumnDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Add column definitions size assertion error: "), actual.getAddColumnDefinitions().size(), is(expected.getAddColumns().size()));
        int count = 0;
        for (AddColumnDefinitionSegment each : actual.getAddColumnDefinitions()) {
            ExpectedAddColumnDefinition expectedAddColumnDefinition = expected.getAddColumns().get(count);
            assertColumnDefinitions(assertContext, each.getColumnDefinitions(), expectedAddColumnDefinition.getColumnDefinitions());
            if (each.getColumnPosition().isPresent()) {
                assertNotNull(assertContext.getText("Column position should exist."), expectedAddColumnDefinition.getColumnPosition());
                ColumnPositionAssert.assertIs(assertContext, each.getColumnPosition().get(), expectedAddColumnDefinition.getColumnPosition());
            } else {
                assertNull(assertContext.getText("Column position should not exist."), expectedAddColumnDefinition.getColumnPosition());
            }
            count++;
        }
    }
    
    private static void assertColumnDefinitions(final SQLCaseAssertContext assertContext, final Collection<ColumnDefinitionSegment> actual, final List<ExpectedColumnDefinition> expected) {
        int count = 0;
        for (ColumnDefinitionSegment each : actual) {
            ColumnDefinitionAssert.assertIs(assertContext, each, expected.get(count));
            count++;
        }
    }
    
    private static void assertAddConstraintDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        int count = 0;
        for (ConstraintDefinitionSegment each : actual.getAddConstraintDefinitions()) {
            ConstraintDefinitionAssert.assertIs(assertContext, each, expected.getAddConstraints().get(count));
            count++;
        }
    }
    
    private static void assertModifyColumnDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Modify column definitions size assertion error: "), actual.getModifyColumnDefinitions().size(), is(expected.getModifyColumns().size()));
        int count = 0;
        for (ModifyColumnDefinitionSegment each : actual.getModifyColumnDefinitions()) {
            ExpectedModifyColumnDefinition expectedModifyColumnDefinition = expected.getModifyColumns().get(count);
            ColumnDefinitionAssert.assertIs(assertContext, each.getColumnDefinition(), expectedModifyColumnDefinition.getColumnDefinition());
            if (each.getColumnPosition().isPresent()) {
                assertNotNull(assertContext.getText("Column position should exist."), expectedModifyColumnDefinition.getColumnPosition());
                ColumnPositionAssert.assertIs(assertContext, each.getColumnPosition().get(), expectedModifyColumnDefinition.getColumnPosition());
            } else {
                assertNull(assertContext.getText("Column position should not exist."), expectedModifyColumnDefinition.getColumnPosition());
            }
            count++;
        }
    }
    
    private static void assertDropColumns(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        Collection<ColumnSegment> actualDropColumns = getDropColumns(actual);
        assertThat(assertContext.getText("Drop columns size assertion error: "), actualDropColumns.size(), is(expected.getDropColumns().size()));
        int count = 0;
        for (ColumnSegment each : actualDropColumns) {
            ColumnAssert.assertIs(assertContext, each, expected.getDropColumns().get(count));
            count++;
        }
    }
    
    private static Collection<ColumnSegment> getDropColumns(final AlterTableStatement actual) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (DropColumnDefinitionSegment each : actual.getDropColumnDefinitions()) {
            result.addAll(each.getColumns());
        }
        return result;
    }
}
