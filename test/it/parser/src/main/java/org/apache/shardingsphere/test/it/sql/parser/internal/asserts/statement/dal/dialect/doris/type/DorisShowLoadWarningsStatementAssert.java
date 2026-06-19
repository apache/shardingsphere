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
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowLoadWarningsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowLoadWarningsStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Show load warnings statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowLoadWarningsStatementAssert {
    
    /**
     * Assert show load warnings statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show load warnings statement
     * @param expected expected show load warnings statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowLoadWarningsStatement actual, final DorisShowLoadWarningsStatementTestCase expected) {
        if (null != expected.getDatabase()) {
            assertNotNull(actual.getDatabase().orElse(null), assertContext.getText("Actual database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getDatabase().get(), expected.getDatabase());
        }
        if (null != expected.getConditionType()) {
            assertNotNull(actual.getConditionType(), assertContext.getText("Actual condition type should exist."));
            assertThat(assertContext.getText("Condition type does not match: "), actual.getConditionType().get(), is(expected.getConditionType()));
        }
        if (null != expected.getConditionValue()) {
            assertNotNull(actual.getConditionValue(), assertContext.getText("Actual condition value should exist."));
            assertThat(assertContext.getText("Condition value does not match: "), actual.getConditionValue().get(), is(expected.getConditionValue()));
        }
    }
}
