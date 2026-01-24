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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ViewColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.SelectStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.view.CreateViewStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Create view statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateViewStatementAssert {
    
    /**
     * Assert create view statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create view statement
     * @param expected expected create view statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateViewStatement actual, final CreateViewStatementTestCase expected) {
        assertIfNotExists(assertContext, actual, expected);
        assertView(assertContext, actual, expected);
        assertViewColumns(assertContext, actual, expected);
        assertViewComment(assertContext, actual, expected);
        assertViewDefinition(assertContext, actual, expected);
        assertSelect(assertContext, actual, expected);
    }
    
    private static void assertIfNotExists(final SQLCaseAssertContext assertContext, final CreateViewStatement actual, final CreateViewStatementTestCase expected) {
        if (null != expected.getIfNotExists()) {
            assertThat(assertContext.getText("IF NOT EXISTS assertion error: "), actual.isIfNotExists(), is(expected.getIfNotExists()));
        }
    }
    
    private static void assertView(final SQLCaseAssertContext assertContext, final CreateViewStatement actual, final CreateViewStatementTestCase expected) {
        TableAssert.assertIs(assertContext, actual.getView(), expected.getView());
    }
    
    private static void assertViewColumns(final SQLCaseAssertContext assertContext, final CreateViewStatement actual, final CreateViewStatementTestCase expected) {
        if (expected.getView().getColumns().isEmpty()) {
            assertTrue(actual.getColumns().isEmpty(), assertContext.getText("Actual view columns should be empty."));
        } else {
            assertFalse(actual.getColumns().isEmpty(), assertContext.getText("Actual view columns should exist."));
            ViewColumnAssert.assertIs(assertContext, actual.getColumns(), expected.getView().getColumns());
        }
    }
    
    private static void assertViewComment(final SQLCaseAssertContext assertContext, final CreateViewStatement actual, final CreateViewStatementTestCase expected) {
        if (null == expected.getComment()) {
            assertNull(actual.getComment(), assertContext.getText("Actual view comment should not exist."));
        } else {
            assertNotNull(actual.getComment(), assertContext.getText("Actual view comment should exist."));
            assertThat(assertContext.getText("View comment assertion error: "), actual.getComment(), is(expected.getComment()));
        }
    }
    
    private static void assertViewDefinition(final SQLCaseAssertContext assertContext, final CreateViewStatement actual, final CreateViewStatementTestCase expected) {
        if (null == expected.getViewDefinition()) {
            assertNull(actual.getViewDefinition(), "actual view definition should not exist");
        } else {
            assertNotNull(actual.getViewDefinition(), "actual view definition should exist");
            assertThat(assertContext.getText(String.format("`%s`'s view definition assertion error: ", actual.getClass().getSimpleName())), actual.getViewDefinition(),
                    is(expected.getViewDefinition()));
        }
    }
    
    private static void assertSelect(final SQLCaseAssertContext assertContext, final CreateViewStatement actual, final CreateViewStatementTestCase expected) {
        if (null == expected.getSelectStatement()) {
            assertNull(actual.getSelect(), "actual select statement should not exist");
        } else {
            assertNotNull(actual.getSelect(), "actual select statement should exist");
            SelectStatementAssert.assertIs(assertContext, actual.getSelect(), expected.getSelectStatement());
        }
    }
}
