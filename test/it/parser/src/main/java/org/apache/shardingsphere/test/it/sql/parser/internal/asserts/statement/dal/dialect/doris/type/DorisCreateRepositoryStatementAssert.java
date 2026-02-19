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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCreateRepositoryStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCreateRepositoryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;

/**
 * Create repository statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisCreateRepositoryStatementAssert {
    
    /**
     * Assert create repository statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create repository statement
     * @param expected expected create repository statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisCreateRepositoryStatement actual, final DorisCreateRepositoryStatementTestCase expected) {
        assertReadOnlyFlag(assertContext, actual, expected);
        assertRepositoryName(assertContext, actual, expected);
        assertStorageType(assertContext, actual, expected);
        assertLocation(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
    }
    
    private static void assertReadOnlyFlag(final SQLCaseAssertContext assertContext, final DorisCreateRepositoryStatement actual, final DorisCreateRepositoryStatementTestCase expected) {
        MatcherAssert.assertThat(assertContext.getText("read-only flag does not match: "), actual.isReadOnly(), Matchers.is(expected.isReadOnly()));
    }
    
    private static void assertRepositoryName(final SQLCaseAssertContext assertContext, final DorisCreateRepositoryStatement actual, final DorisCreateRepositoryStatementTestCase expected) {
        if (null != expected.getRepositoryName()) {
            Assertions.assertNotNull(actual.getRepositoryName(), assertContext.getText("Actual repository name should exist."));
            IdentifierValueAssert.assertIs(assertContext, actual.getRepositoryName().getIdentifier(), expected.getRepositoryName(), "Repository name");
            SQLSegmentAssert.assertIs(assertContext, actual.getRepositoryName(), expected.getRepositoryName());
        }
    }
    
    private static void assertStorageType(final SQLCaseAssertContext assertContext, final DorisCreateRepositoryStatement actual, final DorisCreateRepositoryStatementTestCase expected) {
        if (null != expected.getStorageType()) {
            MatcherAssert.assertThat(assertContext.getText("storage type does not match: "), actual.getStorageType(), Matchers.is(expected.getStorageType()));
        }
    }
    
    private static void assertLocation(final SQLCaseAssertContext assertContext, final DorisCreateRepositoryStatement actual, final DorisCreateRepositoryStatementTestCase expected) {
        if (null != expected.getLocation()) {
            MatcherAssert.assertThat(assertContext.getText("location does not match: "), actual.getLocation(), Matchers.is(expected.getLocation()));
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final DorisCreateRepositoryStatement actual, final DorisCreateRepositoryStatementTestCase expected) {
        Assertions.assertNotNull(actual.getProperties(), assertContext.getText("properties should not be null"));
        if (!expected.getProperties().isEmpty()) {
            MatcherAssert.assertThat(assertContext.getText("Properties size does not match: "), actual.getProperties().getProperties().size(), Matchers.is(expected.getProperties().size()));
            for (int i = 0; i < expected.getProperties().size(); i++) {
                assertProperty(assertContext, actual.getProperties().getProperties().get(i), expected.getProperties().get(i));
            }
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        MatcherAssert.assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), Matchers.is(expected.getKey()));
        MatcherAssert.assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), Matchers.is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
