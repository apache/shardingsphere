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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterViewStatementHandler;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dml.impl.SelectStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.ddl.AlterViewStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Alter view statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterViewStatementAssert {
    
    /**
     * Assert alter view statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual alter view statement
     * @param expected expected alter view statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterViewStatement actual, final AlterViewStatementTestCase expected) {
        assertView(assertContext, actual, expected);
        assertViewDefinition(assertContext, actual, expected);
        assertSelect(assertContext, actual, expected);
    }
    
    private static void assertView(final SQLCaseAssertContext assertContext, final AlterViewStatement actual, final AlterViewStatementTestCase expected) {
        TableAssert.assertIs(assertContext, actual.getView(), expected.getView());
    }
    
    private static void assertViewDefinition(final SQLCaseAssertContext assertContext, final AlterViewStatement actual, final AlterViewStatementTestCase expected) {
        if (null == expected.getViewDefinition()) {
            assertFalse("actual view definition should not exist", AlterViewStatementHandler.getViewDefinition(actual).isPresent());
        } else {
            assertTrue("actual view definition should exist", AlterViewStatementHandler.getViewDefinition(actual).isPresent());
            assertThat(assertContext.getText(String.format("`%s`'s view definition assertion error: ", actual.getClass().getSimpleName())), AlterViewStatementHandler.getViewDefinition(actual).get(),
                    is(expected.getViewDefinition()));
        }
    }
    
    private static void assertSelect(final SQLCaseAssertContext assertContext, final AlterViewStatement actual, final AlterViewStatementTestCase expected) {
        if (null == expected.getSelectStatement()) {
            assertFalse("actual select statement should not exist", AlterViewStatementHandler.getSelectStatement(actual).isPresent());
        } else {
            assertTrue("actual select statement should exist", AlterViewStatementHandler.getSelectStatement(actual).isPresent());
            SelectStatementAssert.assertIs(assertContext, AlterViewStatementHandler.getSelectStatement(actual).get(), expected.getSelectStatement());
        }
    }
}
