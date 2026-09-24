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
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAdminCheckTabletStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAdminCheckTabletStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Admin check tablet statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisAdminCheckTabletStatementAssert {
    
    /**
     * Assert admin check tablet statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual admin check tablet statement
     * @param expected expected admin check tablet statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisAdminCheckTabletStatement actual, final DorisAdminCheckTabletStatementTestCase expected) {
        assertThat(assertContext.getText("Tablet IDs size does not match: "), actual.getTabletIds().size(), is(expected.getTabletIds().size()));
        for (int i = 0; i < expected.getTabletIds().size(); i++) {
            assertThat(assertContext.getText("Tablet ID does not match: "), actual.getTabletIds().get(i), is(expected.getTabletIds().get(i)));
        }
        if (!expected.getProperties().isEmpty()) {
            assertThat(assertContext.getText("Properties size does not match: "), actual.getProperties().getProperties().size(), is(expected.getProperties().size()));
            for (int i = 0; i < expected.getProperties().size(); i++) {
                assertProperty(assertContext, actual.getProperties().getProperties().get(i), expected.getProperties().get(i));
            }
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
