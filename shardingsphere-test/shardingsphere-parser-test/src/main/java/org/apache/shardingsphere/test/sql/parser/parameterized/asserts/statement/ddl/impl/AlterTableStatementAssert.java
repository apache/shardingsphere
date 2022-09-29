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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.ConvertTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.charset.CharsetAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.definition.ColumnDefinitionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.definition.ColumnPositionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.definition.ConstraintDefinitionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.definition.ExpectedAddColumnDefinition;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.definition.ExpectedChangeColumnDefinition;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.definition.ExpectedColumnDefinition;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.definition.ExpectedModifyColumnDefinition;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterTableStatementTestCase;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

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
        assertRenameTable(assertContext, actual, expected);
        assertAddColumnDefinitions(assertContext, actual, expected);
        assertAddConstraintDefinitions(assertContext, actual, expected);
        assertModifyColumnDefinitions(assertContext, actual, expected);
        assertChangeColumnDefinitions(assertContext, actual, expected);
        assertDropColumns(assertContext, actual, expected);
        assertConvertTable(assertContext, actual, expected);
    }
    
    private static void assertConvertTable(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        Optional<ConvertTableDefinitionSegment> convertTable = actual.getConvertTableDefinition();
        if (null != expected.getConvertTable()) {
            assertTrue(assertContext.getText("Actual convert table segment should exist."), convertTable.isPresent());
            CharsetAssert.assertIs(assertContext, convertTable.get().getCharsetName(), expected.getConvertTable().getCharsetName());
            if (null == expected.getConvertTable().getCollateExpression()) {
                assertNull(assertContext.getText("Actual collate expression should not exist."), convertTable.get().getCollateValue());
            } else {
                ExpressionAssert.assertExpression(assertContext, convertTable.get().getCollateValue(), expected.getConvertTable().getCollateExpression().getCollateName());
            }
            SQLSegmentAssert.assertIs(assertContext, convertTable.get(), expected.getConvertTable());
        } else {
            assertFalse(assertContext.getText("Actual convert table segment should not exist."), convertTable.isPresent());
        }
    }
    
    private static void assertRenameTable(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        Optional<SimpleTableSegment> tableSegment = actual.getRenameTable();
        if (null == expected.getRenameTable()) {
            assertFalse(assertContext.getText("Actual table segment should not exist."), tableSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual table segment should exist."), tableSegment.isPresent());
            TableAssert.assertIs(assertContext, tableSegment.get(), expected.getRenameTable());
        }
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
        for (AddConstraintDefinitionSegment each : actual.getAddConstraintDefinitions()) {
            ConstraintDefinitionAssert.assertIs(assertContext, each.getConstraintDefinition(), expected.getAddConstraints().get(count));
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
    
    private static void assertChangeColumnDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Change column definitions size assertion error: "), actual.getChangeColumnDefinitions().size(), is(expected.getChangeColumns().size()));
        int count = 0;
        for (ChangeColumnDefinitionSegment each : actual.getChangeColumnDefinitions()) {
            ExpectedChangeColumnDefinition expectedChangeColumnDefinition = expected.getChangeColumns().get(count);
            ColumnDefinitionAssert.assertIs(assertContext, each.getColumnDefinition(), expectedChangeColumnDefinition.getColumnDefinition());
            if (each.getColumnPosition().isPresent()) {
                assertNotNull(assertContext.getText("Column position should exist."), expectedChangeColumnDefinition.getColumnPosition());
                ColumnPositionAssert.assertIs(assertContext, each.getColumnPosition().get(), expectedChangeColumnDefinition.getColumnPosition());
            } else {
                assertNull(assertContext.getText("Column position should not exist."), expectedChangeColumnDefinition.getColumnPosition());
            }
            if (null != each.getPreviousColumn()) {
                ColumnAssert.assertIs(assertContext, each.getPreviousColumn(), expectedChangeColumnDefinition.getPreviousColumn());
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
