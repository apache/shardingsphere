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
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.definition.ColumnDefinitionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.definition.ConstraintDefinitionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
}
