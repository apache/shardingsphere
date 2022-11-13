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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedEncryptColumn;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedEncryptRule;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Encrypt rule assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptRuleAssert {
    
    /**
     * Assert encrypt rule is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual encrypt rule
     * @param expected expected encrypt rule test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final EncryptRuleSegment actual, final ExpectedEncryptRule expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual encrypt rule should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual encrypt rule should exist."), actual);
            assertThat(assertContext.getText("encrypt rule assertion error: "), actual.getTableName(), is(expected.getName()));
            assertThat(assertContext.getText("encrypt rule assertion error: "), actual.getQueryWithCipherColumn(), is(expected.getQueryWithCipherColumn()));
            assertEncryptColumns(assertContext, actual.getColumns(), expected.getColumns());
        }
    }
    
    private static void assertEncryptColumns(final SQLCaseAssertContext assertContext, final Collection<EncryptColumnSegment> actual, final List<ExpectedEncryptColumn> expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual encrypt column should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual encrypt column should exist."), actual);
            assertThat(assertContext.getText(String.format("Actual encrypt column size should be %s , but it was %s", expected.size(), actual.size())), actual.size(), is(expected.size()));
            int count = 0;
            for (EncryptColumnSegment encryptColumnSegment : actual) {
                ExpectedEncryptColumn expectedEncryptColumn = expected.get(count);
                EncryptColumnAssert.assertIs(assertContext, encryptColumnSegment, expectedEncryptColumn);
                count++;
            }
        }
    }
}
