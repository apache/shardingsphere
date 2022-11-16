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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.HintRALStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintTableValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.SetShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.SetReadwriteSplittingHintStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ShowReadwriteSplittingHintStatusStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ShowShardingHintStatusStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ClearReadwriteSplittingHintStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.hint.AddShardingHintDatabaseValueStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.hint.AddShardingHintTableValueStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.hint.SetShardingHintDatabaseValueStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.hint.SetReadwriteSplittingHintStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.hint.ShowReadwriteSplittingHintStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.hint.ShowShardingHintStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.hint.ClearReadwriteSplittingHintStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.AddShardingHintDatabaseValueStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.AddShardingHintTableValueStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.SetShardingHintDatabaseValueStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.SetReadwriteSplittingHintStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.ShowReadwriteSplittingHintStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.ShowShardingHintStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.ClearReadwriteSplittingHintStatementTestCase;

/**
 * Hint RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintRALStatementAssert {
    
    /**
     * Assert Hint RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual hint RAL statement
     * @param expected expected hint RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final HintRALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof AddShardingHintTableValueStatement) {
            AddShardingHintTableValueStatementAssert.assertIs(assertContext, (AddShardingHintTableValueStatement) actual, (AddShardingHintTableValueStatementTestCase) expected);
        } else if (actual instanceof AddShardingHintDatabaseValueStatement) {
            AddShardingHintDatabaseValueStatementAssert.assertIs(assertContext, (AddShardingHintDatabaseValueStatement) actual, (AddShardingHintDatabaseValueStatementTestCase) expected);
        } else if (actual instanceof SetShardingHintDatabaseValueStatement) {
            SetShardingHintDatabaseValueStatementAssert.assertIs(assertContext, (SetShardingHintDatabaseValueStatement) actual, (SetShardingHintDatabaseValueStatementTestCase) expected);
        } else if (actual instanceof SetReadwriteSplittingHintStatement) {
            SetReadwriteSplittingHintStatementAssert.assertIs(assertContext, (SetReadwriteSplittingHintStatement) actual, (SetReadwriteSplittingHintStatementTestCase) expected);
        } else if (actual instanceof ShowReadwriteSplittingHintStatusStatement) {
            ShowReadwriteSplittingHintStatusStatementAssert.assertIs(assertContext, (ShowReadwriteSplittingHintStatusStatement) actual, (ShowReadwriteSplittingHintStatusStatementTestCase) expected);
        } else if (actual instanceof ShowShardingHintStatusStatement) {
            ShowShardingHintStatusStatementAssert.assertIs(assertContext, (ShowShardingHintStatusStatement) actual, (ShowShardingHintStatusStatementTestCase) expected);
        } else if (actual instanceof ClearReadwriteSplittingHintStatement) {
            ClearReadwriteSplittingHintStatementAssert.assertIs(assertContext, (ClearReadwriteSplittingHintStatement) actual, (ClearReadwriteSplittingHintStatementTestCase) expected);
        }
    }
}
