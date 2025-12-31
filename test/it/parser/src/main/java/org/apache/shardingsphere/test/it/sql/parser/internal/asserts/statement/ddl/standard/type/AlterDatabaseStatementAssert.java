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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.database.AlterDatabaseStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Alter database statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterDatabaseStatementAssert {
    
    /**
     * Assert alter database statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter database statement
     * @param expected expected alter database statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterDatabaseStatement actual, final AlterDatabaseStatementTestCase expected) {
        assertDatabaseName(assertContext, actual, expected);
        assertRenameDatabaseName(assertContext, actual, expected);
        assertQuota(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
    }
    
    private static void assertDatabaseName(final SQLCaseAssertContext assertContext, final AlterDatabaseStatement actual, final AlterDatabaseStatementTestCase expected) {
        if (null != expected.getDatabaseName()) {
            assertNotNull(actual.getDatabaseName(), assertContext.getText("Database name should not be null"));
            assertThat(assertContext.getText("Database name does not match: "), actual.getDatabaseName(), is(expected.getDatabaseName()));
        }
    }
    
    private static void assertRenameDatabaseName(final SQLCaseAssertContext assertContext, final AlterDatabaseStatement actual, final AlterDatabaseStatementTestCase expected) {
        if (null != expected.getRenameDatabaseName()) {
            assertThat(assertContext.getText("Rename database name does not match: "), actual.getRenameDatabaseName().orElse(null), is(expected.getRenameDatabaseName()));
        }
    }
    
    private static void assertQuota(final SQLCaseAssertContext assertContext, final AlterDatabaseStatement actual, final AlterDatabaseStatementTestCase expected) {
        if (null != expected.getQuotaType()) {
            assertThat(assertContext.getText("Quota type does not match: "), actual.getQuotaType().orElse(null), is(expected.getQuotaType()));
        }
        if (null != expected.getQuotaValue()) {
            assertThat(assertContext.getText("Quota value does not match: "), actual.getQuotaValue().orElse(null), is(expected.getQuotaValue()));
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final AlterDatabaseStatement actual, final AlterDatabaseStatementTestCase expected) {
        if (expected.getProperties().isEmpty()) {
            return;
        }
        assertNotNull(actual.getProperties(), assertContext.getText("Properties should not be null"));
        assertThat(assertContext.getText("Properties size does not match: "), actual.getProperties().getProperties().size(), is(expected.getProperties().size()));
        for (int i = 0; i < expected.getProperties().size(); i++) {
            assertProperty(assertContext, actual.getProperties().getProperties().get(i), expected.getProperties().get(i));
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
