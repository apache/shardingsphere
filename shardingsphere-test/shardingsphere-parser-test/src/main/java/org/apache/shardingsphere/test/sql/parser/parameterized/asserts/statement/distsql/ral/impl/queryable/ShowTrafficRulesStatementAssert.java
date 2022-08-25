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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowTrafficRulesStatementTestCase;
import org.apache.shardingsphere.traffic.distsql.parser.statement.queryable.ShowTrafficRulesStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Show traffic rules statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowTrafficRulesStatementAssert {
    
    /**
     * Assert show traffic rules statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show traffic rules statement
     * @param expected expected show traffic rules statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowTrafficRulesStatement actual, final ShowTrafficRulesStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertThat(assertContext.getText("Rule name id assertion error"), actual.getRuleName(), is(expected.getRuleName()));
        }
    }
}
