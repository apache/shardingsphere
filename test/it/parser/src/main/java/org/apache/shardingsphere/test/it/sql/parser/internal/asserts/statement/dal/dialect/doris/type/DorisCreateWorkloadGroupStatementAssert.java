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
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCreateWorkloadGroupStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCreateWorkloadGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Create workload group statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisCreateWorkloadGroupStatementAssert {
    
    /**
     * Assert create workload group statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create workload group statement
     * @param expected expected create workload group statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisCreateWorkloadGroupStatement actual, final DorisCreateWorkloadGroupStatementTestCase expected) {
        assertNotNull(expected.getGroup(), assertContext.getText("expected create workload group should be not null"));
        assertThat(assertContext.getText("group name does not match: "), actual.getGroupName(), is(expected.getGroup().getName()));
        if (expected.isIfNotExists()) {
            assertThat(assertContext.getText("IF NOT EXISTS flag assertion error: "), actual.isIfNotExists(), is(true));
        }
        assertNotNull(actual.getProperties(), assertContext.getText("Properties should not be null"));
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
