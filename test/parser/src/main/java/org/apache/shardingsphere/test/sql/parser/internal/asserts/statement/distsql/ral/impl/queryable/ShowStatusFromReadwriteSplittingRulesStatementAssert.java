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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.queryable;

import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.distsql.ral.ShowStatusFromReadwriteSplittingRulesStatementTestCase;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Show status from readwrite-splitting rules statement assert.
 */
public final class ShowStatusFromReadwriteSplittingRulesStatementAssert {
    
    /**
     * Assert show readwrite-splitting read resources statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show status from readwrite-splitting rules statement
     * @param expected expected show status from readwrite-splitting rules statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowStatusFromReadwriteSplittingRulesStatement actual,
                                final ShowStatusFromReadwriteSplittingRulesStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
        }
    }
}
