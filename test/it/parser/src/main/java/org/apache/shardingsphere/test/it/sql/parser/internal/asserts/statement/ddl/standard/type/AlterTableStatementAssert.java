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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.AddPartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.AddPartitionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.ModifyPartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.PartitionValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.RenamePartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.distribution.ModifyDistributionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.engine.ModifyEngineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.feature.EnableFeatureSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.AddRollupDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.DropRollupDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ModifyTableCommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.primary.DropPrimaryKeyDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.RenameRollupDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ConvertTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ReplaceTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
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
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.partition.PartitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.rollup.RollupAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedAddColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedChangeColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedConstraintDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedModifyColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenameColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenameIndexDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedAddPartitionDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedModifyPartitionDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenamePartitionDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedAddRollupDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedDropRollupDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenameRollupDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedEnableFeatureDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedModifyEngineDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedModifyTableCommentDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedModifyDistributionDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.partition.ExpectedAddPartitions;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperties;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.partition.ExpectedPartitionValues;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table.AlterTableStatementTestCase;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
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
        assertReplaceTable(assertContext, actual, expected);
        assertAddColumnDefinitions(assertContext, actual, expected);
        assertAddConstraintDefinitions(assertContext, actual, expected);
        assertModifyConstraintDefinitions(assertContext, actual, expected);
        assertModifyColumnDefinitions(assertContext, actual, expected);
        assertChangeColumnDefinitions(assertContext, actual, expected);
        assertDropColumns(assertContext, actual, expected);
        assertRenameIndexDefinitions(assertContext, actual, expected);
        assertRenameColumnDefinitions(assertContext, actual, expected);
        assertAddRollupDefinitions(assertContext, actual, expected);
        assertDropRollupDefinitions(assertContext, actual, expected);
        assertRenameRollupDefinitions(assertContext, actual, expected);
        assertRenamePartitionDefinitions(assertContext, actual, expected);
        assertAddPartitionDefinitions(assertContext, actual, expected);
        assertModifyPartitionDefinitions(assertContext, actual, expected);
        assertAddPartitionsSegments(assertContext, actual, expected);
        assertConvertTable(assertContext, actual, expected);
        assertModifyCollectionRetrievalDefinitions(assertContext, actual, expected);
        assertDropPrimaryKeyDefinition(assertContext, actual, expected);
        assertSetPropertiesDefinitions(assertContext, actual, expected);
        assertEnableFeatureDefinitions(assertContext, actual, expected);
        assertModifyTableCommentDefinitions(assertContext, actual, expected);
        assertModifyEngineDefinitions(assertContext, actual, expected);
        assertModifyDistributionDefinitions(assertContext, actual, expected);
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
    
    private static void assertReplaceTable(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        Optional<ReplaceTableDefinitionSegment> replaceTableSegment = actual.getReplaceTable();
        if (null == expected.getReplaceTable()) {
            assertFalse(replaceTableSegment.isPresent(), assertContext.getText("Actual replace table segment should not exist."));
        } else {
            assertTrue(replaceTableSegment.isPresent(), assertContext.getText("Actual replace table segment should exist."));
            ReplaceTableDefinitionSegment actualReplaceTable = replaceTableSegment.get();
            assertNotNull(actualReplaceTable.getReplaceTable(), assertContext.getText("Actual replace table should exist."));
            TableAssert.assertIs(assertContext, actualReplaceTable.getReplaceTable(), expected.getReplaceTable().getTable());
            if (null == expected.getReplaceTable().getProperties()) {
                assertNull(actualReplaceTable.getProperties(), assertContext.getText("Actual properties should not exist."));
            } else {
                assertNotNull(actualReplaceTable.getProperties(), assertContext.getText("Actual properties should exist."));
                assertProperties(assertContext, actualReplaceTable.getProperties(), expected.getReplaceTable().getProperties());
            }
            SQLSegmentAssert.assertIs(assertContext, actualReplaceTable, expected.getReplaceTable());
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final PropertiesSegment actual, final ExpectedProperties expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        assertThat(assertContext.getText("Properties size assertion error: "), actual.getProperties().size(), is(expected.getProperties().size()));
        for (int i = 0; i < expected.getProperties().size(); i++) {
            assertProperty(assertContext, actual.getProperties().get(i), expected.getProperties().get(i));
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final ExpectedProperty expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
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
        int actualColumnCount = 0;
        for (DropColumnDefinitionSegment each : actual.getDropColumnDefinitions()) {
            actualColumnCount += each.getColumns().size();
        }
        assertThat(assertContext.getText("Drop column definitions size assertion error: "), actualColumnCount, is(expected.getDropColumns().size()));
        int count = 0;
        for (DropColumnDefinitionSegment each : actual.getDropColumnDefinitions()) {
            for (ColumnSegment column : each.getColumns()) {
                ExpectedColumn expectedColumn = expected.getDropColumns().get(count);
                ColumnAssert.assertIs(assertContext, column, expectedColumn);
                if (null != expectedColumn.getProperties()) {
                    assertTrue(each.getProperties().isPresent(), assertContext.getText("Drop column properties should exist"));
                    assertProperties(assertContext, each.getProperties().get(), expectedColumn.getProperties());
                }
                count++;
            }
        }
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
    
    private static void assertAddRollupDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Add rollup definitions size assertion error: "), actual.getAddRollupDefinitions().size(), is(expected.getAddRollups().size()));
        int count = 0;
        for (AddRollupDefinitionSegment each : actual.getAddRollupDefinitions()) {
            ExpectedAddRollupDefinition expectedAddRollupDefinition = expected.getAddRollups().get(count);
            RollupAssert.assertIs(assertContext, each.getRollupSegment(), expectedAddRollupDefinition.getRollup());
            assertThat(assertContext.getText("Add rollup columns size assertion error: "), each.getColumns().size(), is(expectedAddRollupDefinition.getColumns().size()));
            int columnCount = 0;
            for (ColumnSegment columnSegment : each.getColumns()) {
                ColumnAssert.assertIs(assertContext, columnSegment, expectedAddRollupDefinition.getColumns().get(columnCount));
                columnCount++;
            }
            if (null != expectedAddRollupDefinition.getFromIndex()) {
                assertTrue(each.getFromIndex().isPresent(), assertContext.getText("Add rollup from index should exist"));
                IndexAssert.assertIs(assertContext, each.getFromIndex().get(), expectedAddRollupDefinition.getFromIndex());
            } else {
                assertFalse(each.getFromIndex().isPresent(), assertContext.getText("Add rollup from index should not exist"));
            }
            if (null != expectedAddRollupDefinition.getProperties()) {
                assertTrue(each.getProperties().isPresent(), assertContext.getText("Add rollup properties should exist"));
                assertProperties(assertContext, each.getProperties().get(), expectedAddRollupDefinition.getProperties());
            } else {
                assertFalse(each.getProperties().isPresent(), assertContext.getText("Add rollup properties should not exist"));
            }
            SQLSegmentAssert.assertIs(assertContext, each, expectedAddRollupDefinition);
            count++;
        }
    }
    
    private static void assertDropRollupDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Drop rollup definitions size assertion error: "), actual.getDropRollupDefinitions().size(), is(expected.getDropRollups().size()));
        int count = 0;
        for (DropRollupDefinitionSegment each : actual.getDropRollupDefinitions()) {
            ExpectedDropRollupDefinition expectedDropRollupDefinition = expected.getDropRollups().get(count);
            RollupAssert.assertIs(assertContext, each.getRollupSegment(), expectedDropRollupDefinition.getRollup());
            if (null != expectedDropRollupDefinition.getProperties()) {
                assertTrue(each.getProperties().isPresent(), assertContext.getText("Drop rollup properties should exist"));
                assertProperties(assertContext, each.getProperties().get(), expectedDropRollupDefinition.getProperties());
            } else {
                assertFalse(each.getProperties().isPresent(), assertContext.getText("Drop rollup properties should not exist"));
            }
            SQLSegmentAssert.assertIs(assertContext, each, expectedDropRollupDefinition);
            count++;
        }
    }
    
    private static void assertRenameRollupDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Rename rollup definitions size assertion error: "), actual.getRenameRollupDefinitions().size(), is(expected.getRenameRollups().size()));
        int count = 0;
        for (RenameRollupDefinitionSegment each : actual.getRenameRollupDefinitions()) {
            ExpectedRenameRollupDefinition expectedRenameRollupDefinition = expected.getRenameRollups().get(count);
            RollupAssert.assertIs(assertContext, each.getRollupSegment(), expectedRenameRollupDefinition.getOldRollup());
            RollupAssert.assertIs(assertContext, each.getRenameRollupSegment(), expectedRenameRollupDefinition.getNewRollup());
            SQLSegmentAssert.assertIs(assertContext, each, expectedRenameRollupDefinition);
            count++;
        }
    }
    
    private static void assertRenamePartitionDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Rename partition definitions size assertion error: "), actual.getRenamePartitionDefinitions().size(), is(expected.getRenamePartitions().size()));
        int count = 0;
        for (RenamePartitionDefinitionSegment each : actual.getRenamePartitionDefinitions()) {
            ExpectedRenamePartitionDefinition expectedRenamePartitionDefinition = expected.getRenamePartitions().get(count);
            PartitionAssert.assertIs(assertContext, each.getPartitionSegment(), expectedRenamePartitionDefinition.getOldPartition());
            PartitionAssert.assertIs(assertContext, each.getRenamePartitionSegment(), expectedRenamePartitionDefinition.getNewPartition());
            SQLSegmentAssert.assertIs(assertContext, each, expectedRenamePartitionDefinition);
            count++;
        }
    }
    
    private static void assertAddPartitionDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Add partition definitions size assertion error: "), actual.getAddPartitionDefinitions().size(), is(expected.getAddPartitions().size()));
        int count = 0;
        for (AddPartitionDefinitionSegment each : actual.getAddPartitionDefinitions()) {
            ExpectedAddPartitionDefinition expectedAddPartition = expected.getAddPartitions().get(count);
            PartitionAssert.assertIs(assertContext, each.getPartition(), expectedAddPartition.getPartition());
            if (each.getPartitionValues().isPresent()) {
                assertNotNull(expectedAddPartition.getPartitionValues(), assertContext.getText("Expected partition values should exist."));
                assertPartitionValues(assertContext, each.getPartitionValues().get(), expectedAddPartition.getPartitionValues());
            } else {
                assertNull(expectedAddPartition.getPartitionValues(), assertContext.getText("Expected partition values should not exist."));
            }
            if (each.getProperties().isPresent()) {
                assertNotNull(expectedAddPartition.getProperties(), assertContext.getText("Expected properties should exist."));
                assertProperties(assertContext, each.getProperties().get(), expectedAddPartition.getProperties());
            } else {
                assertNull(expectedAddPartition.getProperties(), assertContext.getText("Expected properties should not exist."));
            }
            if (each.getDistributedColumn().isPresent()) {
                assertNotNull(expectedAddPartition.getDistributedColumn(), assertContext.getText("Expected distributed column should exist."));
                ColumnAssert.assertIs(assertContext, each.getDistributedColumn().get(), expectedAddPartition.getDistributedColumn());
            } else {
                assertNull(expectedAddPartition.getDistributedColumn(), assertContext.getText("Expected distributed column should not exist."));
            }
            if (each.getBuckets().isPresent()) {
                assertNotNull(expectedAddPartition.getBuckets(), assertContext.getText("Expected buckets should exist."));
                assertThat(assertContext.getText("Buckets value assertion error: "), each.getBuckets().get(), is(Integer.parseInt(expectedAddPartition.getBuckets().getValue())));
            } else {
                assertNull(expectedAddPartition.getBuckets(), assertContext.getText("Expected buckets should not exist."));
            }
            SQLSegmentAssert.assertIs(assertContext, each, expectedAddPartition);
            count++;
        }
    }
    
    private static void assertPartitionValues(final SQLCaseAssertContext assertContext, final PartitionValuesSegment actual, final ExpectedPartitionValues expected) {
        assertThat(assertContext.getText("Partition values type assertion error: "), actual.getValuesType().name(), is(expected.getType()));
        if (null != expected.getIsMaxValue()) {
            assertThat(assertContext.getText("Partition values max value assertion error: "), actual.isMaxValue(), is(expected.getIsMaxValue()));
        }
        assertThat(assertContext.getText("Partition values size assertion error: "), actual.getValues().size(), is(expected.getValues().size()));
        int count = 0;
        for (ExpressionSegment each : actual.getValues()) {
            ExpressionAssert.assertExpression(assertContext, each, expected.getValues().get(count));
            count++;
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertModifyPartitionDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Modify partition definitions size assertion error: "), actual.getModifyPartitionDefinitions().size(), is(expected.getModifyPartitions().size()));
        int count = 0;
        for (ModifyPartitionDefinitionSegment each : actual.getModifyPartitionDefinitions()) {
            ExpectedModifyPartitionDefinition expectedModifyPartition = expected.getModifyPartitions().get(count);
            if (null != expectedModifyPartition.getAllPartitions()) {
                assertThat(assertContext.getText("Modify partition all partitions assertion error: "), each.isAllPartitions(), is(expectedModifyPartition.getAllPartitions()));
            }
            assertThat(assertContext.getText("Modify partition partitions size assertion error: "), each.getPartitions().size(), is(expectedModifyPartition.getPartitions().size()));
            int partitionCount = 0;
            for (PartitionSegment partition : each.getPartitions()) {
                PartitionAssert.assertIs(assertContext, partition, expectedModifyPartition.getPartitions().get(partitionCount));
                partitionCount++;
            }
            assertNotNull(each.getProperties(), assertContext.getText("Actual properties should exist."));
            assertNotNull(expectedModifyPartition.getProperties(), assertContext.getText("Expected properties should exist."));
            assertProperties(assertContext, each.getProperties(), expectedModifyPartition.getProperties());
            SQLSegmentAssert.assertIs(assertContext, each, expectedModifyPartition);
            count++;
        }
    }
    
    private static void assertAddPartitionsSegments(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Add partitions segments size assertion error: "), actual.getAddPartitionsSegments().size(), is(expected.getAddPartitionsList().size()));
        int count = 0;
        for (AddPartitionsSegment each : actual.getAddPartitionsSegments()) {
            ExpectedAddPartitions expectedAddPartitions = expected.getAddPartitionsList().get(count);
            ExpressionAssert.assertExpression(assertContext, each.getFromValue(), expectedAddPartitions.getFromValue());
            ExpressionAssert.assertExpression(assertContext, each.getToValue(), expectedAddPartitions.getToValue());
            ExpressionAssert.assertExpression(assertContext, each.getIntervalValue(), expectedAddPartitions.getIntervalValue());
            if (each.getIntervalUnit().isPresent()) {
                assertNotNull(expectedAddPartitions.getIntervalUnit(), assertContext.getText("Expected interval unit should exist."));
                assertThat(assertContext.getText("Interval unit value assertion error: "), each.getIntervalUnit().get().getIdentifier().getValue(),
                        is(expectedAddPartitions.getIntervalUnit().getName()));
                SQLSegmentAssert.assertIs(assertContext, each.getIntervalUnit().get(), expectedAddPartitions.getIntervalUnit());
            } else {
                assertNull(expectedAddPartitions.getIntervalUnit(), assertContext.getText("Expected interval unit should not exist."));
            }
            SQLSegmentAssert.assertIs(assertContext, each, expectedAddPartitions);
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
    
    private static void assertSetPropertiesDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Set properties definitions size assertion error: "), actual.getSetPropertiesDefinitions().size(), is(expected.getSetProperties().size()));
        int count = 0;
        for (PropertiesSegment each : actual.getSetPropertiesDefinitions()) {
            ExpectedProperties expectedProperties = expected.getSetProperties().get(count);
            assertProperties(assertContext, each, expectedProperties);
            SQLSegmentAssert.assertIs(assertContext, each, expectedProperties);
            count++;
        }
    }
    
    private static void assertEnableFeatureDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Enable feature definitions size assertion error: "), actual.getEnableFeatureDefinitions().size(), is(expected.getEnableFeatures().size()));
        int count = 0;
        for (EnableFeatureSegment each : actual.getEnableFeatureDefinitions()) {
            ExpectedEnableFeatureDefinition expectedEnableFeature = expected.getEnableFeatures().get(count);
            assertThat(assertContext.getText("Feature name assertion error: "), each.getFeatureName(), is(expectedEnableFeature.getFeatureName()));
            if (null != expectedEnableFeature.getProperties()) {
                assertTrue(each.getProperties().isPresent(), assertContext.getText("Enable feature properties should exist"));
                assertProperties(assertContext, each.getProperties().get(), expectedEnableFeature.getProperties());
            } else {
                assertFalse(each.getProperties().isPresent(), assertContext.getText("Enable feature properties should not exist"));
            }
            SQLSegmentAssert.assertIs(assertContext, each, expectedEnableFeature);
            count++;
        }
    }
    
    private static void assertModifyTableCommentDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Modify table comment definitions size assertion error: "), actual.getModifyTableCommentDefinitions().size(), is(expected.getModifyTableComments().size()));
        int count = 0;
        for (ModifyTableCommentSegment each : actual.getModifyTableCommentDefinitions()) {
            ExpectedModifyTableCommentDefinition expectedModifyTableComment = expected.getModifyTableComments().get(count);
            assertThat(assertContext.getText("Table comment assertion error: "), each.getTableComment(), is(expectedModifyTableComment.getTableComment()));
            SQLSegmentAssert.assertIs(assertContext, each, expectedModifyTableComment);
            count++;
        }
    }
    
    private static void assertModifyEngineDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Modify engine definitions size assertion error: "), actual.getModifyEngineDefinitions().size(), is(expected.getModifyEngines().size()));
        int count = 0;
        for (ModifyEngineSegment each : actual.getModifyEngineDefinitions()) {
            ExpectedModifyEngineDefinition expectedModifyEngine = expected.getModifyEngines().get(count);
            assertThat(assertContext.getText("Engine type assertion error: "), each.getEngineType(), is(expectedModifyEngine.getEngineType()));
            if (null != expectedModifyEngine.getProperties()) {
                assertTrue(each.getProperties().isPresent(), assertContext.getText("Modify engine properties should exist"));
                assertProperties(assertContext, each.getProperties().get(), expectedModifyEngine.getProperties());
            } else {
                assertFalse(each.getProperties().isPresent(), assertContext.getText("Modify engine properties should not exist"));
            }
            SQLSegmentAssert.assertIs(assertContext, each, expectedModifyEngine);
            count++;
        }
    }
    
    private static void assertModifyDistributionDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Modify distribution definitions size assertion error: "), actual.getModifyDistributionDefinitions().size(), is(expected.getModifyDistributions().size()));
        int count = 0;
        for (ModifyDistributionSegment each : actual.getModifyDistributionDefinitions()) {
            ExpectedModifyDistributionDefinition expectedModifyDistribution = expected.getModifyDistributions().get(count);
            assertThat(assertContext.getText("Modify distribution columns size assertion error: "), each.getColumns().size(), is(expectedModifyDistribution.getColumns().size()));
            int columnCount = 0;
            for (ColumnSegment columnSegment : each.getColumns()) {
                ColumnAssert.assertIs(assertContext, columnSegment, expectedModifyDistribution.getColumns().get(columnCount));
                columnCount++;
            }
            if (null != expectedModifyDistribution.getBuckets()) {
                assertNotNull(each.getBuckets(), assertContext.getText("Modify distribution buckets should exist"));
                assertThat(assertContext.getText("Buckets assertion error: "), each.getBuckets(), is(expectedModifyDistribution.getBuckets()));
            } else {
                assertNull(each.getBuckets(), assertContext.getText("Modify distribution buckets should not exist"));
            }
            SQLSegmentAssert.assertIs(assertContext, each, expectedModifyDistribution);
            count++;
        }
    }
}
