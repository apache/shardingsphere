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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.updatable;

import org.apache.shardingsphere.parser.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.parser.distsql.parser.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.segment.impl.distsql.ExpectedCacheOption;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.distsql.ral.AlterSQLParserRuleStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Alter SQL parser rule statement assert.
 */
public final class AlterSQLParserRuleStatementAssert {
    
    /**
     * Assert alter SQL parser rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter SQL parser rule statement
     * @param expected expected alter SQL parser rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterSQLParserRuleStatement actual, final AlterSQLParserRuleStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
        }
        assertNotNull(actual);
        assertCacheOption(assertContext, actual.getParseTreeCache(), expected.getSqlParserRule().getParseTreeCache());
        assertCacheOption(assertContext, actual.getSqlStatementCache(), expected.getSqlParserRule().getSqlStatementCache());
    }
    
    private static void assertCacheOption(final SQLCaseAssertContext assertContext, final CacheOptionSegment actual, final ExpectedCacheOption expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
        }
        assertNotNull(actual);
        assertThat(actual.getInitialCapacity(), is(expected.getInitialCapacity()));
        assertThat(actual.getMaximumSize(), is(expected.getMaximumSize()));
    }
}
