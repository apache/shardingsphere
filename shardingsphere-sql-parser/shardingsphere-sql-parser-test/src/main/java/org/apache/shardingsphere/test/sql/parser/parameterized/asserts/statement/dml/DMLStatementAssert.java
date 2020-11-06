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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.*;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl.*;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.*;

/**
 * DML statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DMLStatementAssert {

    /**
     * Assert DML statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual DML statement
     * @param expected      expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DMLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof SelectStatement) {
            SelectStatementAssert.assertIs(assertContext, (SelectStatement) actual, (SelectStatementTestCase) expected);
        } else if (actual instanceof UpdateStatement) {
            UpdateStatementAssert.assertIs(assertContext, (UpdateStatement) actual, (UpdateStatementTestCase) expected);
        } else if (actual instanceof DeleteStatement) {
            DeleteStatementAssert.assertIs(assertContext, (DeleteStatement) actual, (DeleteStatementTestCase) expected);
        } else if (actual instanceof InsertStatement) {
            InsertStatementAssert.assertIs(assertContext, (InsertStatement) actual, (InsertStatementTestCase) expected);
        } else if (actual instanceof CallStatement) {
            CallStatementAssert.assertIs(assertContext, (CallStatement) actual, (CallStatementTestCase) expected);
        }
    }
}
