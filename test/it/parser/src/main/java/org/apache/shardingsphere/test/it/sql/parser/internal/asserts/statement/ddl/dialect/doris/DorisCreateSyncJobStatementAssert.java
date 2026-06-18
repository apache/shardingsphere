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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.ChannelDescriptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateSyncJobStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.job.ExpectedChannelDescription;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisCreateSyncJobStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Create sync job statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisCreateSyncJobStatementAssert {
    
    /**
     * Assert create sync job statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create sync job statement
     * @param expected expected create sync job statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisCreateSyncJobStatement actual, final DorisCreateSyncJobStatementTestCase expected) {
        assertJobName(assertContext, actual, expected);
        assertChannelDescriptions(assertContext, actual, expected);
        assertBinlogDescription(assertContext, actual, expected);
    }
    
    private static void assertJobName(final SQLCaseAssertContext assertContext, final DorisCreateSyncJobStatement actual, final DorisCreateSyncJobStatementTestCase expected) {
        if (actual.getJobName().isPresent()) {
            assertThat(assertContext.getText("Job name does not match: "), actual.getJobName().get().getIdentifier().getValue(), is(expected.getJobName()));
            if (null != expected.getOwner()) {
                OwnerAssert.assertIs(assertContext, actual.getJobName().get().getOwner().orElse(null), expected.getOwner());
            }
        }
    }
    
    private static void assertChannelDescriptions(final SQLCaseAssertContext assertContext, final DorisCreateSyncJobStatement actual, final DorisCreateSyncJobStatementTestCase expected) {
        assertThat(assertContext.getText("Channel descriptions size does not match: "), actual.getChannelDescriptions().size(), is(expected.getChannelDescriptions().size()));
        int index = 0;
        for (ChannelDescriptionSegment actualChannel : actual.getChannelDescriptions()) {
            assertChannelDescription(assertContext, actualChannel, expected.getChannelDescriptions().get(index));
            index++;
        }
    }
    
    private static void assertChannelDescription(final SQLCaseAssertContext assertContext, final ChannelDescriptionSegment actual, final ExpectedChannelDescription expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        TableAssert.assertIs(assertContext, actual.getSourceTable(), expected.getSourceTable());
        TableAssert.assertIs(assertContext, actual.getTargetTable(), expected.getTargetTable());
        assertThat(assertContext.getText("Column names size assertion error: "), actual.getColumnNames().size(), is(expected.getColumnNames().size()));
        int index = 0;
        for (String actualColumnName : actual.getColumnNames()) {
            assertThat(assertContext.getText(String.format("Column name at index %d assertion error: ", index)), actualColumnName, is(expected.getColumnNames().get(index)));
            index++;
        }
    }
    
    private static void assertBinlogDescription(final SQLCaseAssertContext assertContext, final DorisCreateSyncJobStatement actual, final DorisCreateSyncJobStatementTestCase expected) {
        if (actual.getBinlogDescription().isPresent() && null != expected.getBinlogDescription()) {
            SQLSegmentAssert.assertIs(assertContext, actual.getBinlogDescription().get(), expected.getBinlogDescription());
            assertNotNull(actual.getBinlogDescription().get().getProperties(), assertContext.getText("Binlog properties should not be null"));
            if (!expected.getBinlogDescription().getProperties().isEmpty()) {
                assertThat(assertContext.getText("Binlog properties size does not match: "), actual.getBinlogDescription().get().getProperties().getProperties().size(),
                        is(expected.getBinlogDescription().getProperties().size()));
                for (int i = 0; i < expected.getBinlogDescription().getProperties().size(); i++) {
                    assertProperty(assertContext, actual.getBinlogDescription().get().getProperties().getProperties().get(i), expected.getBinlogDescription().getProperties().get(i));
                }
            }
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
