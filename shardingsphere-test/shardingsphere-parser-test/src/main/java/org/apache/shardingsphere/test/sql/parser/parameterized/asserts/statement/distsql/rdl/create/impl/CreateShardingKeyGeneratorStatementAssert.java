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
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingKeyGeneratorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingKeyGeneratorStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedShardingKeyGenerator;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingKeyGeneratorStatementTestCase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Create sharding key generator statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateShardingKeyGeneratorStatementAssert {
    
    /**
     * Assert create sharding key generator statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create sharding key generator statement
     * @param expected expected create sharding key generator statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateShardingKeyGeneratorStatement actual, final CreateShardingKeyGeneratorStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertShardingKeyGenerator(assertContext, actual.getKeyGeneratorSegments(), expected.getShardingKeyGenerators());
        }
    }
    
    private static void assertShardingKeyGenerator(final SQLCaseAssertContext assertContext, final Collection<ShardingKeyGeneratorSegment> actual, final List<ExpectedShardingKeyGenerator> expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual sharding key generator should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual sharding key generator should exist."), actual);
            final Map<String, ShardingKeyGeneratorSegment> actualMap = actual.stream().collect(Collectors.toMap(ShardingKeyGeneratorSegment::getKeyGeneratorName, each -> each));
            for (ExpectedShardingKeyGenerator each : expected) {
                ShardingKeyGeneratorSegment actualAlgorithm = actualMap.get(each.getKeyGeneratorName());
                assertNotNull(assertContext.getText("Actual sharding key generator should exist."), actualAlgorithm);
                assertThat(actualAlgorithm.getKeyGeneratorName(), is(each.getKeyGeneratorName()));
                AlgorithmAssert.assertIs(assertContext, actualAlgorithm.getAlgorithmSegment(), each.getAlgorithmSegment());
            }
        }
    }
}
