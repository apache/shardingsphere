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
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowSqlBlockRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowSqlBlockRuleStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Show SQL block rule statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowSqlBlockRuleStatementAssert {
    
    /**
     * Assert show SQL block rule statement.
     *
     * @param assertContext assert context
     * @param actual actual show SQL block rule statement
     * @param expected expected show SQL block rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowSqlBlockRuleStatement actual, final DorisShowSqlBlockRuleStatementTestCase expected) {
        if (null != expected.getRuleName() && actual.getRuleName().isPresent()) {
            assertThat(assertContext.getText("Rule name does not match: "), actual.getRuleName().get().getIdentifier().getValue(), is(expected.getRuleName().getName()));
            SQLSegmentAssert.assertIs(assertContext, actual.getRuleName().get(), expected.getRuleName());
        }
    }
}
