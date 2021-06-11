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

package org.apache.shardingsphere.distsql.parser.api.asserts.rdl.impl.alter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.SQLCaseAssertContext;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.alter.AlterReadWriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterReadwriteSplittingRuleStatement;

/**
 * Alter readwrite splitting rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterReadwriteSplittingRuleStatementAssert {

    /**
     * Assert alter readwrite splitting rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter readwrite splitting rule statement
     * @param expected expected alter readwrite splitting rule statement result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterReadwriteSplittingRuleStatement actual, final AlterReadWriteSplittingRuleStatementTestCase expected) {
    }
}
