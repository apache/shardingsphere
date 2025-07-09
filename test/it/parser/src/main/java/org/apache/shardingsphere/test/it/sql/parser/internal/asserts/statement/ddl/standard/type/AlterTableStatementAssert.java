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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyCollectionRetrievalSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.ModifyConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.RenameIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.primary.DropPrimaryKeyDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ConvertTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.charset.CharsetAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.definition.ColumnDefinitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.definition.ColumnPositionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.definition.ConstraintDefinitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.definition.IndexDefinitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedAddColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedChangeColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedConstraintDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedModifyColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenameColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenameIndexDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table.AlterTableStatementTestCase;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertModifyConstraintDefinitions(assertContext, actual, expected);
        assertModifyColumnDefinitions(assertContext, actual, expected);
        assertChangeColumnDefinitions(assertContext, actual, expected);
        assertDropColumns(assertContext, actual, expected);
        assertRenameIndexDefinitions(assertContext, actual, expected);
        assertRenameColumnDefinitions(assertContext, actual, expected);
        assertConvertTable(assertContext, actual, expected);
        assertModifyCollectionRetrievalDefinitions(assertContext, actual, expected);
        assertDropPrimaryKeyDefinition(assertContext, actual, expected);
    }
    
    private static void assertConvertTable(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        Optional<ConvertTableDefinitionSegment> convertTable = actual.getConvertTableDefinition();
        if (null == expected.getConvertTable()) {
            assertFalse(convertTable.isPresent(), assertContext.getText("Actual convert table segment should not exist."));
        } else {
            assertTrue(convertTable.isPresent(), assertContext.getText("Actual convert table segment should exist."));
            CharsetAssert.assertIs(assertContext, convertTable.get().getCharsetName(), expected.getConvertTable().getCharsetName());
            if (null == expected.getConvertTable().getCollateExpression()) {
                assertNull(convertTable.get().getCollateValue(), assertContext.getText("Actual collate expression should not exist."));
            } else {
                ExpressionAssert.assertExpression(assertContext, convertTable.get().getCollateValue(), expected.getConvertTable().getCollateExpression().getCollateName());
            }
            SQLSegmentAssert.assertIs(assertContext, convertTable.get(), expected.getConvertTable());
        }
    }
    
    private static void assertRenameTable(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        Optional<SimpleTableSegment> tableSegment = actual.getRenameTable();
        if (null == expected.getRenameTable()) {
            assertFalse(tableSegment.isPresent(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertTrue(tableSegment.isPresent(), assertContext.getText("Actual table segment should exist."));
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
                assertNotNull(expectedAddColumnDefinition.getColumnPosition(), assertContext.getText("Column position should exist."));
                ColumnPositionAssert.assertIs(assertContext, each.getColumnPosition().get(), expectedAddColumnDefinition.getColumnPosition());
            } else {
                assertNull(expectedAddColumnDefinition.getColumnPosition(), assertContext.getText("Column position should not exist."));
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
    
    private static void assertModifyConstraintDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Modify constraint definitions size assertion error: "), actual.getModifyConstraintDefinitions().size(), is(expected.getModifyConstraints().size()));
        int count = 0;
        for (ModifyConstraintDefinitionSegment each : actual.getModifyConstraintDefinitions()) {
            ExpectedConstraintDefinition expectedConstraintDefinition = expected.getModifyConstraints().get(count);
            if (null == expectedConstraintDefinition.getConstraintName()) {
                assertNull(each.getConstraintName(), "Actual modify constraint name should not exist.");
            } else {
                assertNotNull(each.getConstraintName(), "Actual modify constraint name should exist.");
                assertThat(assertContext.getText("Actual modify constraint name assertion error."),
                        each.getConstraintName().getIdentifier().getValue(), is(expectedConstraintDefinition.getConstraintName()));
            }
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
                assertNotNull(expectedModifyColumnDefinition.getColumnPosition(), assertContext.getText("Column position should exist."));
                ColumnPositionAssert.assertIs(assertContext, each.getColumnPosition().get(), expectedModifyColumnDefinition.getColumnPosition());
            } else {
                assertNull(expectedModifyColumnDefinition.getColumnPosition(), assertContext.getText("Column position should not exist."));
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
                assertNotNull(expectedChangeColumnDefinition.getColumnPosition(), assertContext.getText("Column position should exist."));
                ColumnPositionAssert.assertIs(assertContext, each.getColumnPosition().get(), expectedChangeColumnDefinition.getColumnPosition());
            } else {
                assertNull(expectedChangeColumnDefinition.getColumnPosition(), assertContext.getText("Column position should not exist."));
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
    
    private static void assertRenameIndexDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Rename index definitions size assertion error: "), actual.getRenameIndexDefinitions().size(), is(expected.getRenameIndexes().size()));
        int count = 0;
        for (RenameIndexDefinitionSegment each : actual.getRenameIndexDefinitions()) {
            ExpectedRenameIndexDefinition expectedRenameIndexDefinition = expected.getRenameIndexes().get(count);
            IndexDefinitionAssert.assertIs(assertContext, each.getIndexSegment(), expectedRenameIndexDefinition.getIndexDefinition());
            IndexDefinitionAssert.assertIs(assertContext, each.getRenameIndexSegment(), expectedRenameIndexDefinition.getRenameIndexDefinition());
            count++;
        }
    }
    
    private static void assertRenameColumnDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Rename columns definitions size assertion error:"), actual.getRenameColumnDefinitions().size(), is(expected.getRenameColumns().size()));
        int count = 0;
        for (RenameColumnSegment each : actual.getRenameColumnDefinitions()) {
            ExpectedRenameColumnDefinition expectedRenameColumnDefinition = expected.getRenameColumns().get(count);
            ColumnAssert.assertIs(assertContext, each.getOldColumnName(), expectedRenameColumnDefinition.getOldColumnName());
            ColumnAssert.assertIs(assertContext, each.getColumnName(), expectedRenameColumnDefinition.getColumnName());
            count++;
        }
    }
    
    private static void assertModifyCollectionRetrievalDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        Optional<ModifyCollectionRetrievalSegment> modifyCollectionRetrieval = actual.getModifyCollectionRetrieval();
        if (null == expected.getModifyCollectionRetrievalDefinition()) {
            assertFalse(modifyCollectionRetrieval.isPresent(), assertContext.getText("Actual modify collection retrieval definitions should not exist."));
        } else {
            assertTrue(modifyCollectionRetrieval.isPresent(), assertContext.getText("Actual modify collection retrieval definitions should exist."));
            if (null == expected.getModifyCollectionRetrievalDefinition().getTable()) {
                assertNull(modifyCollectionRetrieval.get().getNestedTable(), "Actual nested table should not exist.");
            } else {
                assertNotNull(modifyCollectionRetrieval.get().getNestedTable(), "Actual nested table should exist.");
                TableAssert.assertIs(assertContext, modifyCollectionRetrieval.get().getNestedTable(), expected.getModifyCollectionRetrievalDefinition().getTable());
            }
        }
    }
    
    private static void assertDropPrimaryKeyDefinition(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        Optional<DropPrimaryKeyDefinitionSegment> dropPrimaryKeyDefinition = actual.getDropPrimaryKeyDefinition();
        if (!dropPrimaryKeyDefinition.isPresent()) {
            return;
        }
        assertNotNull(expected.getDropPrimaryKeyDefinition(), assertContext.getText("Actual drop primary key definition should exist."));
        SQLSegmentAssert.assertIs(assertContext, actual.getDropPrimaryKeyDefinition().get(), expected.getDropPrimaryKeyDefinition());
    }
}
