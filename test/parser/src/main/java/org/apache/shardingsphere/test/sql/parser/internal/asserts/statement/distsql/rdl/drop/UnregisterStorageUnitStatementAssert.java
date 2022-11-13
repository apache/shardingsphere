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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.rdl.drop;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.rdl.drop.UnregisterStorageUnitStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unregister storage unit statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnregisterStorageUnitStatementAssert {
    
    /**
     * Assert drop resource statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual unregister storage unit statement
     * @param expected expected unregister storage unit statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final UnregisterStorageUnitStatement actual, final UnregisterStorageUnitStatementTestCase expected) {
        if (null == expected.getDataSources()) {
            assertNull(assertContext.getText("Actual resource should not exist."), actual);
        } else {
            assertThat(assertContext.getText("resource assertion error: "), actual.getNames(), is(expected.getDataSources()));
            assertThat(assertContext.getText("resource assertion error: "), actual.isIgnoreSingleTables(), is(expected.getIgnoreSingleTables().iterator().next()));
            assertThat(assertContext.getText("resource assertion error: "), actual.isIfExists(), is(expected.isIfExists()));
        }
    }
}
