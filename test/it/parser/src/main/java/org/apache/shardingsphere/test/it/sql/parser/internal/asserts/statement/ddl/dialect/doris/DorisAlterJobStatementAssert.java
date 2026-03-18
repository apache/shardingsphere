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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterJobStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterJobStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Alter job statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisAlterJobStatementAssert {
    
    /**
     * Assert alter job statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter job statement
     * @param expected expected alter job statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisAlterJobStatement actual, final DorisAlterJobStatementTestCase expected) {
        assertJobName(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
        assertInsertStatement(assertContext, actual, expected);
    }
    
    private static void assertJobName(final SQLCaseAssertContext assertContext, final DorisAlterJobStatement actual, final DorisAlterJobStatementTestCase expected) {
        assertNotNull(actual.getJobName(), assertContext.getText("Job name should exist."));
        assertThat(assertContext.getText("Job name does not match: "), actual.getJobName().getIdentifier().getValue(), is(expected.getJobName().getName()));
        SQLSegmentAssert.assertIs(assertContext, actual.getJobName(), expected.getJobName());
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final DorisAlterJobStatement actual, final DorisAlterJobStatementTestCase expected) {
        if (!expected.getProperties().isEmpty()) {
            assertTrue(actual.getProperties().isPresent(), assertContext.getText("Properties should exist."));
            assertThat(assertContext.getText("Properties size does not match: "), actual.getProperties().get().getProperties().size(), is(expected.getProperties().size()));
            for (int i = 0; i < expected.getProperties().size(); i++) {
                assertProperty(assertContext, actual.getProperties().get().getProperties().get(i), expected.getProperties().get(i));
            }
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertInsertStatement(final SQLCaseAssertContext assertContext, final DorisAlterJobStatement actual, final DorisAlterJobStatementTestCase expected) {
        if (null == expected.getInsertTable() && null == expected.getInsertSelectTable()) {
            return;
        }
        assertTrue(actual.getInsertStatement().isPresent(), assertContext.getText("Insert statement should exist."));
        if (null != expected.getInsertTable()) {
            assertTrue(actual.getInsertStatement().get().getTable().isPresent(), assertContext.getText("Insert target table should exist."));
            TableAssert.assertIs(assertContext, actual.getInsertStatement().get().getTable().get(), expected.getInsertTable());
        }
        if (null != expected.getInsertSelectTable()) {
            assertTrue(actual.getInsertStatement().get().getInsertSelect().isPresent(), assertContext.getText("Insert select should exist."));
            SimpleTableSegment fromTable = (SimpleTableSegment) actual.getInsertStatement().get().getInsertSelect().get().getSelect().getFrom().orElse(null);
            assertNotNull(fromTable, assertContext.getText("Insert select from table should exist."));
            TableAssert.assertIs(assertContext, fromTable, expected.getInsertSelectTable());
        }
    }
}
