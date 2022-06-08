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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.UpdatableScalingRALStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ApplyScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.DropScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ResetScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.RestoreScalingSourceWritingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingSourceWritingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StartScalingStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.update.ApplyScalingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.update.DropScalingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.update.ResetScalingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.update.RestoreScalingSourceWritingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.update.StopScalingSourceWritingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.update.StopScalingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.update.StartScalingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.ApplyScalingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.ResetScalingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.DropScalingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.RestoreScalingSourceWritingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.StopScalingSourceWritingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.StopScalingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.StartScalingStatementTestCase;

/**
 * Updatable Scaling RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdatableScalingRALStatementAssert {
    
    /**
     * Assert updatable scaling RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual updatable scaling RAL statement
     * @param expected expected updatable scaling RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final UpdatableScalingRALStatement actual, final SQLParserTestCase expected) {
        // TODO add more test case
        if (actual instanceof StopScalingSourceWritingStatement) {
            StopScalingSourceWritingStatementAssert.assertIs(assertContext, (StopScalingSourceWritingStatement) actual, (StopScalingSourceWritingStatementTestCase) expected);
        } else if (actual instanceof RestoreScalingSourceWritingStatement) {
            RestoreScalingSourceWritingStatementAssert.assertIs(assertContext, (RestoreScalingSourceWritingStatement) actual, (RestoreScalingSourceWritingStatementTestCase) expected);
        } else if (actual instanceof ApplyScalingStatement) {
            ApplyScalingStatementAssert.assertIs(assertContext, (ApplyScalingStatement) actual, (ApplyScalingStatementTestCase) expected);
        } else if (actual instanceof StopScalingStatement) {
            StopScalingStatementAssert.assertIs(assertContext, (StopScalingStatement) actual, (StopScalingStatementTestCase) expected);
        } else if (actual instanceof ResetScalingStatement) {
            ResetScalingStatementAssert.assertIs(assertContext, (ResetScalingStatement) actual, (ResetScalingStatementTestCase) expected);
        } else if (actual instanceof DropScalingStatement) {
            DropScalingStatementAssert.assertIs(assertContext, (DropScalingStatement) actual, (DropScalingStatementTestCase) expected);
        } else if (actual instanceof StartScalingStatement) {
            StartScalingStatementAssert.assertIs(assertContext, (StartScalingStatement) actual, (StartScalingStatementTestCase) expected);
        }
    }
}
