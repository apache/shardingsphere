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

package org.apache.shardingsphere.distsql.parser.api.asserts.statement.rql.impl.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.api.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rql.ShowShardingTableRulesStatementTestCase;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowShardingTableRulesStatement;

/**
 * Show sharding table rules statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowShardingTableRulesStatementAssert {
    
    /**
     * Assert show database discovery rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual show database discovery rules statement
     * @param expected      expected show database discovery rules statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowShardingTableRulesStatement actual, final ShowShardingTableRulesStatementTestCase expected) {
    }
}
