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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.DistSQLStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;

/**
 * Abstract sql statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AbstractSQLStatementAssert {

    /**
     * Assert abstract sql statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual abstract sql statement
     * @param expected expected abstract sql statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof DistSQLStatement) {
            DistSQLStatementAssert.assertIs(assertContext, (DistSQLStatement) actual, expected);
        }
    }
}
