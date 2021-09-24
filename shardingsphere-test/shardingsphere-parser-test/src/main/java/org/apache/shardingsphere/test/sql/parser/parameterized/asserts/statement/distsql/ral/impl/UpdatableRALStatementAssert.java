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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableRALStatement;
import org.apache.shardingsphere.scaling.distsql.statement.CheckoutScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingSourceWritingStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.update.CheckoutScalingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.update.StopScalingSourceWritingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.CheckoutScalingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.StopScalingSourceWritingStatementTestCase;

/**
 * Updatable RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdatableRALStatementAssert {

    /**
     * Assert updatable RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual updatable RAL statement
     * @param expected expected updatable RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final UpdatableRALStatement actual, final SQLParserTestCase expected) {
        // TODO add more test case
        if (actual instanceof StopScalingSourceWritingStatement) {
            StopScalingSourceWritingStatementAssert.assertIs(assertContext, (StopScalingSourceWritingStatement) actual, (StopScalingSourceWritingStatementTestCase) expected);
        } else if (actual instanceof CheckoutScalingStatement) {
            CheckoutScalingStatementAssert.assertIs(assertContext, (CheckoutScalingStatement) actual, (CheckoutScalingStatementTestCase) expected);
        }
    }
}
