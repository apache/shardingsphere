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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.workloadgroup.WorkloadGroupNameSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterWorkloadGroupStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedCatalogProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterWorkloadGroupStatementTestCase;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Alter workload group statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisAlterWorkloadGroupStatementAssert {
    
    /**
     * Assert alter workload group statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter workload group statement
     * @param expected expected alter workload group statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisAlterWorkloadGroupStatement actual, final DorisAlterWorkloadGroupStatementTestCase expected) {
        assertWorkloadGroupName(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
    }
    
    private static void assertWorkloadGroupName(final SQLCaseAssertContext assertContext, final DorisAlterWorkloadGroupStatement actual, final DorisAlterWorkloadGroupStatementTestCase expected) {
        if (null != expected.getWorkloadGroupName()) {
            WorkloadGroupNameSegment actualSegment = actual.getWorkloadGroupName();
            assertThat(assertContext.getText("Workload group name assertion error: "), actualSegment.getName(), is(expected.getWorkloadGroupName().getName()));
            SQLSegmentAssert.assertIs(assertContext, actualSegment, expected.getWorkloadGroupName());
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final DorisAlterWorkloadGroupStatement actual, final DorisAlterWorkloadGroupStatementTestCase expected) {
        if (null == expected.getProperties()) {
            return;
        }
        PropertiesSegment actualProperties = actual.getProperties();
        SQLSegmentAssert.assertIs(assertContext, actualProperties, expected.getProperties());
        assertThat(assertContext.getText("Properties size assertion error: "), actualProperties.getProperties().size(), is(expected.getProperties().getProperties().size()));
        for (int i = 0; i < expected.getProperties().getProperties().size(); i++) {
            assertProperty(assertContext, actualProperties.getProperties().get(i), expected.getProperties().getProperties().get(i));
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final ExpectedCatalogProperty expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
