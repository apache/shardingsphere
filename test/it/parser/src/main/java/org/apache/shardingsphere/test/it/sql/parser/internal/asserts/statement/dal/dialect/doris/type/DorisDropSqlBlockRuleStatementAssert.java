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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.RuleNameSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisDropSqlBlockRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisDropSqlBlockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.RuleNameTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Drop SQL block rule statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisDropSqlBlockRuleStatementAssert {
    
    /**
     * Assert drop SQL block rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual drop SQL block rule statement
     * @param expected expected drop SQL block rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisDropSqlBlockRuleStatement actual, final DorisDropSqlBlockRuleStatementTestCase expected) {
        assertThat(assertContext.getText("Rule names size does not match: "), actual.getRuleNames().size(), is(expected.getRuleNames().size()));
        int count = 0;
        for (RuleNameSegment each : actual.getRuleNames()) {
            RuleNameTestCase expectedRuleName = expected.getRuleNames().get(count);
            assertThat(assertContext.getText("Rule name does not match: "), each.getIdentifier().getValue(), is(expectedRuleName.getName()));
            SQLSegmentAssert.assertIs(assertContext, each, expectedRuleName);
            count++;
        }
    }
}
