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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisAlterRoutineLoadStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris.DorisAlterRoutineLoadStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Alter routine load statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisAlterRoutineLoadStatementAssert {
    
    /**
     * Assert alter routine load statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter routine load statement
     * @param expected expected alter routine load statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisAlterRoutineLoadStatement actual, final DorisAlterRoutineLoadStatementTestCase expected) {
        assertJobName(assertContext, actual, expected);
        assertJobProperties(assertContext, actual, expected);
        assertDataSource(assertContext, actual, expected);
        assertDataSourceProperties(assertContext, actual, expected);
    }
    
    private static void assertJobName(final SQLCaseAssertContext assertContext, final DorisAlterRoutineLoadStatement actual, final DorisAlterRoutineLoadStatementTestCase expected) {
        if (actual.getJobName().isPresent()) {
            assertThat(assertContext.getText("Job name does not match: "), actual.getJobName().get().getIdentifier().getValue(), is(expected.getJobName()));
            if (null != expected.getOwner()) {
                OwnerAssert.assertIs(assertContext, actual.getJobName().get().getOwner().orElse(null), expected.getOwner());
            }
        }
    }
    
    private static void assertJobProperties(final SQLCaseAssertContext assertContext, final DorisAlterRoutineLoadStatement actual, final DorisAlterRoutineLoadStatementTestCase expected) {
        if (actual.getJobProperties().isPresent() && !expected.getJobProperties().isEmpty()) {
            assertNotNull(actual.getJobProperties().get(), assertContext.getText("Job properties should not be null"));
            assertThat(assertContext.getText("Job properties size does not match: "), actual.getJobProperties().get().getProperties().size(),
                    is(expected.getJobProperties().size()));
            for (int i = 0; i < expected.getJobProperties().size(); i++) {
                assertProperty(assertContext, actual.getJobProperties().get().getProperties().get(i), expected.getJobProperties().get(i));
            }
        }
    }
    
    private static void assertDataSource(final SQLCaseAssertContext assertContext, final DorisAlterRoutineLoadStatement actual, final DorisAlterRoutineLoadStatementTestCase expected) {
        if (null != expected.getDataSource()) {
            assertThat(assertContext.getText("Data source does not match: "), actual.getDataSource().orElse(null), is(expected.getDataSource()));
        }
    }
    
    private static void assertDataSourceProperties(final SQLCaseAssertContext assertContext, final DorisAlterRoutineLoadStatement actual, final DorisAlterRoutineLoadStatementTestCase expected) {
        if (actual.getDataSourceProperties().isPresent() && !expected.getDataSourceProperties().isEmpty()) {
            assertNotNull(actual.getDataSourceProperties().get(), assertContext.getText("Data source properties should not be null"));
            assertThat(assertContext.getText("Data source properties size does not match: "), actual.getDataSourceProperties().get().getProperties().size(),
                    is(expected.getDataSourceProperties().size()));
            for (int i = 0; i < expected.getDataSourceProperties().size(); i++) {
                assertProperty(assertContext, actual.getDataSourceProperties().get().getProperties().get(i), expected.getDataSourceProperties().get(i));
            }
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
