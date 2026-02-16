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
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateFileStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CreateFileStatementTestCase;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Create file statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateFileStatementAssert {
    
    /**
     * Assert create file statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create file statement
     * @param expected expected create file statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateFileStatement actual, final CreateFileStatementTestCase expected) {
        assertFileName(assertContext, actual, expected);
        assertDatabaseName(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
    }
    
    private static void assertFileName(final SQLCaseAssertContext assertContext, final CreateFileStatement actual, final CreateFileStatementTestCase expected) {
        if (null == expected.getFileName()) {
            assertNull(actual.getFileName(), assertContext.getText("File name should be null"));
        } else {
            assertNotNull(actual.getFileName(), assertContext.getText("File name should not be null"));
            assertThat(assertContext.getText("File name assertion error: "), actual.getFileName(), is(expected.getFileName()));
        }
    }
    
    private static void assertDatabaseName(final SQLCaseAssertContext assertContext, final CreateFileStatement actual, final CreateFileStatementTestCase expected) {
        if (null == expected.getDatabaseName()) {
            assertNull(actual.getDatabaseName(), assertContext.getText("Database name should be null"));
        } else {
            assertNotNull(actual.getDatabaseName(), assertContext.getText("Database name should not be null"));
            assertThat(assertContext.getText("Database name assertion error: "), actual.getDatabaseName(), is(expected.getDatabaseName()));
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final CreateFileStatement actual, final CreateFileStatementTestCase expected) {
        assertNotNull(expected.getProperties(), assertContext.getText("Expected properties should not be null"));
        assertNotNull(actual.getProperties(), assertContext.getText("Actual properties should not be null"));
        SQLSegmentAssert.assertIs(assertContext, actual.getProperties(), expected.getProperties());
        assertThat(assertContext.getText("Properties size assertion error: "),
                actual.getProperties().getProperties().size(), is(expected.getProperties().getProperties().size()));
        for (int i = 0; i < expected.getProperties().getProperties().size(); i++) {
            assertProperty(assertContext, actual.getProperties().getProperties().get(i), expected.getProperties().getProperties().get(i));
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final ExpectedProperty expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
