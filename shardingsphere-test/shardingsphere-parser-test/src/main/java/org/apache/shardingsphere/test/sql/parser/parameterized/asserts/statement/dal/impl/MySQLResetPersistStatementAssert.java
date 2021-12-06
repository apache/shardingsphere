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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLResetPersistStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ResetPersistStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * MySQL reset persist statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLResetPersistStatementAssert {
    
    /**
     * Assert reset persist statement is correct with expected reset persist statement test case.
     *
     * @param assertContext assert context
     * @param actual actual reset persist statement
     * @param expected expected reset persist statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLResetPersistStatement actual, final ResetPersistStatementTestCase expected) {
        assertThat(assertContext.getText("Actual reset persist exist clause does not match: "), actual.isContainsExistClause(), is(expected.isContainsExistClause()));
        if (null != expected.getIdentifier()) {
            assertThat(assertContext.getText("Actual reset persist identifier does not match: "), actual.getIdentifier().getValue(), is(expected.getIdentifier()));
        }
    }
}
