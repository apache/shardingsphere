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
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.QueryableScalingRALStatement;
import org.apache.shardingsphere.scaling.distsql.statement.CheckScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingCheckAlgorithmsStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingListStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingStatusStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.query.CheckScalingStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.query.ShowScalingCheckAlgorithmsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.query.ShowScalingListStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.query.ShowScalingStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowScalingListStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.CheckScalingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.ShowScalingCheckAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.ShowScalingStatusStatementTestCase;

/**
 * Queryable RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryableScalingRALStatementAssert {
    
    /**
     * Assert query RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual queryable RAL statement
     * @param expected expected queryable RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final QueryableScalingRALStatement actual, final SQLParserTestCase expected) {
        // TODO add more test case
        if (actual instanceof ShowScalingListStatement) {
            ShowScalingListStatementAssert.assertIs(assertContext, (ShowScalingListStatement) actual, (ShowScalingListStatementTestCase) expected);
        } else if (actual instanceof ShowScalingCheckAlgorithmsStatement) {
            ShowScalingCheckAlgorithmsStatementAssert.assertIs(assertContext, (ShowScalingCheckAlgorithmsStatement) actual, (ShowScalingCheckAlgorithmsStatementTestCase) expected);
        } else if (actual instanceof CheckScalingStatement) {
            CheckScalingStatementAssert.assertIs(assertContext, (CheckScalingStatement) actual, (CheckScalingStatementTestCase) expected);
        } else if (actual instanceof ShowScalingStatusStatement) {
            ShowScalingStatusStatementAssert.assertIs(assertContext, (ShowScalingStatusStatement) actual, (ShowScalingStatusStatementTestCase) expected);
        }
    }
}
