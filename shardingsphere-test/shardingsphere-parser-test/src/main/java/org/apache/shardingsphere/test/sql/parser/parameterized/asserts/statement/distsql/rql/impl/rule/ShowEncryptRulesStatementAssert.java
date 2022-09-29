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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowEncryptRulesStatementTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * show encrypt rules statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowEncryptRulesStatementAssert {
    
    /**
     * Assert Show encrypt rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show encrypt rules statement
     * @param expected expected show encrypt rules statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowEncryptRulesStatement actual, final ShowEncryptRulesStatementTestCase expected) {
        if (null == expected.getDatabase()) {
            assertFalse(assertContext.getText("Actual database should not exist."), actual.getDatabase().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual database should exist."), actual.getDatabase().isPresent());
            DatabaseAssert.assertIs(assertContext, actual.getDatabase().get(), expected.getDatabase());
        }
    }
}
