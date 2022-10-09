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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingAlgorithmStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedShardingAlgorithm;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingAlgorithmStatementTestCase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Create sharding algorithm statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateShardingAlgorithmStatementAssert {
    
    /**
     * Assert create sharding algorithm statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create sharding algorithm statement
     * @param expected expected create sharding algorithm statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateShardingAlgorithmStatement actual, final CreateShardingAlgorithmStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertShardingAlgorithm(assertContext, actual.getAlgorithmSegments(), expected.getShardingAlgorithms());
        }
    }
    
    private static void assertShardingAlgorithm(final SQLCaseAssertContext assertContext, final Collection<ShardingAlgorithmSegment> actual, final List<ExpectedShardingAlgorithm> expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual sharding algorithm should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual sharding algorithm should exist."), actual);
            final Map<String, ShardingAlgorithmSegment> actualMap = actual.stream().collect(Collectors.toMap(ShardingAlgorithmSegment::getShardingAlgorithmName, each -> each));
            for (ExpectedShardingAlgorithm each : expected) {
                ShardingAlgorithmSegment actualAlgorithm = actualMap.get(each.getShardingAlgorithmName());
                assertNotNull(assertContext.getText("Actual sharding algorithm should exist."), actualAlgorithm);
                assertThat(actualAlgorithm.getShardingAlgorithmName(), is(each.getShardingAlgorithmName()));
                AlgorithmAssert.assertIs(assertContext, actualAlgorithm.getAlgorithmSegment(), each.getAlgorithmSegment());
            }
        }
    }
}
