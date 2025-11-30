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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.resource.UnregisterStorageUnitStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unregister storage unit statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnregisterStorageUnitStatementAssert {
    
    /**
     * Assert unregister storage unit statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual unregister storage unit statement
     * @param expected expected unregister storage unit statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final UnregisterStorageUnitStatement actual, final UnregisterStorageUnitStatementTestCase expected) {
        if (null == expected.getDataSources()) {
            assertNull(actual, assertContext.getText("Actual storage unit should not exist."));
        } else {
            assertThat(assertContext.getText("storage unit assertion error: "), actual.getStorageUnitNames(), is(expected.getDataSources()));
            assertThat(assertContext.getText("storage unit assertion error: "), actual.isIgnoreSingleTables(), is(expected.getIgnoreSingleTables().iterator().next()));
            assertThat(assertContext.getText("storage unit assertion error: "), actual.isIfExists(), is(expected.isIfExists()));
        }
    }
}
