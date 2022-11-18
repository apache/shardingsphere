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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.rdl.create.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateDefaultShardingStrategyStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.distsql.rdl.create.CreateDefaultShardingStrategyStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Create default sharding strategy statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateDefaultShardingStrategyStatementAssert {
    
    /**
     * Assert create default sharding strategy statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create default sharding strategy statement
     * @param expected expected create default sharding strategy statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateDefaultShardingStrategyStatement actual, final CreateDefaultShardingStrategyStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getDefaultType(), is(expected.getStrategy().getDefaultType()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getShardingColumn(), is(expected.getStrategy().getShardingColumn()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getStrategyType(), is(expected.getStrategy().getStrategyType()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getAlgorithmSegment().getName(), is(expected.getStrategy().getAlgorithmSegment().getName()));
        }
    }
}
