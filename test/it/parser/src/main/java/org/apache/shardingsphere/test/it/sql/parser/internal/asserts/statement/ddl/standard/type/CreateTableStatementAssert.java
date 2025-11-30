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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.CreateTableOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.definition.ColumnDefinitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.definition.ConstraintDefinitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.definition.CreateTableOptionDefinitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.SelectStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table.CreateTableStatementTestCase;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Create table statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateTableStatementAssert {
    
    /**
     * Assert create table statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create table statement
     * @param expected expected create table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateTableStatement actual, final CreateTableStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertColumnDefinitions(assertContext, actual, expected);
        assertConstraintDefinitions(assertContext, actual, expected);
        assertCreateTableAsSelectStatement(assertContext, actual, expected);
        assertCreateTableAsSelectStatementColumns(assertContext, actual, expected);
        assertLikeTableStatement(assertContext, actual, expected);
        assertCreateTableOptionStatement(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final CreateTableStatement actual, final CreateTableStatementTestCase expected) {
        TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
    }
    
    private static void assertColumnDefinitions(final SQLCaseAssertContext assertContext, final CreateTableStatement actual, final CreateTableStatementTestCase expected) {
        assertThat(assertContext.getText("Column definitions size assertion error: "), actual.getColumnDefinitions().size(), is(expected.getColumnDefinitions().size()));
        int count = 0;
        for (ColumnDefinitionSegment each : actual.getColumnDefinitions()) {
            ColumnDefinitionAssert.assertIs(assertContext, each, expected.getColumnDefinitions().get(count));
            count++;
        }
    }
    
    private static void assertConstraintDefinitions(final SQLCaseAssertContext assertContext, final CreateTableStatement actual, final CreateTableStatementTestCase expected) {
        assertThat(assertContext.getText("Constraint definitions size assertion error: "), actual.getConstraintDefinitions().size(), is(expected.getConstraintDefinitions().size()));
        int count = 0;
        for (ConstraintDefinitionSegment each : actual.getConstraintDefinitions()) {
            ConstraintDefinitionAssert.assertIs(assertContext, each, expected.getConstraintDefinitions().get(count));
            count++;
        }
    }
    
    private static void assertCreateTableAsSelectStatement(final SQLCaseAssertContext assertContext, final CreateTableStatement actual, final CreateTableStatementTestCase expected) {
        Optional<SelectStatement> selectStatement = actual.getSelectStatement();
        if (null == expected.getCreateTableAsSelectStatement()) {
            assertFalse(selectStatement.isPresent(), "actual select statement should not exist");
        } else {
            assertTrue(selectStatement.isPresent(), "actual select statement should exist");
            SelectStatementAssert.assertIs(assertContext, selectStatement.get(), expected.getCreateTableAsSelectStatement());
        }
    }
    
    private static void assertCreateTableAsSelectStatementColumns(final SQLCaseAssertContext assertContext, final CreateTableStatement actual, final CreateTableStatementTestCase expected) {
        List<ColumnSegment> columns = actual.getColumns();
        assertThat(assertContext.getText("Columns size assertion error: "), columns.size(), is(expected.getColumns().size()));
        int count = 0;
        for (ColumnSegment each : columns) {
            ColumnAssert.assertIs(assertContext, each, expected.getColumns().get(count));
            count++;
        }
    }
    
    private static void assertLikeTableStatement(final SQLCaseAssertContext assertContext, final CreateTableStatement actual, final CreateTableStatementTestCase expected) {
        Optional<SimpleTableSegment> likeTableSegment = actual.getLikeTable();
        if (null == expected.getLikeTable()) {
            assertFalse(likeTableSegment.isPresent(), "actual like table statement should not exist");
        } else {
            assertTrue(likeTableSegment.isPresent(), "actual like table statement should exist");
            TableAssert.assertIs(assertContext, likeTableSegment.get(), expected.getLikeTable());
        }
    }
    
    private static void assertCreateTableOptionStatement(final SQLCaseAssertContext assertContext, final CreateTableStatement actual, final CreateTableStatementTestCase expected) {
        Optional<CreateTableOptionSegment> createTableOption = actual.getCreateTableOption();
        if (null == expected.getCreateTableOption()) {
            assertFalse(createTableOption.isPresent(), assertContext.getText("Actual create table option should not exist."));
        } else {
            assertTrue(createTableOption.isPresent(), assertContext.getText("Actual create table option should exist."));
            CreateTableOptionDefinitionAssert.assertIs(assertContext, createTableOption.get(), expected.getCreateTableOption());
        }
    }
}
