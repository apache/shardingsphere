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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingKeyGeneratorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingKeyGeneratorStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.rdl.ShardingKeyGeneratorAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedShardingKeyGenerator;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterShardingKeyGeneratorStatementTestCase;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Alter sharding key generator statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterShardingKeyGeneratorStatementAssert {
    
    /**
     * Assert alter sharding key generator statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter sharding key generator statement
     * @param expected expected create sharding key generator statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterShardingKeyGeneratorStatement actual, final AlterShardingKeyGeneratorStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual sharding key generator statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual sharding key generator statement should exist."), actual);
            assertShardingKeyGenerator(assertContext, actual.getKeyGeneratorSegments(), expected.getShardingKeyGenerators());
        }
    }
    
    private static void assertShardingKeyGenerator(final SQLCaseAssertContext assertContext, final Collection<ShardingKeyGeneratorSegment> actual, final List<ExpectedShardingKeyGenerator> expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual sharding key generator segments should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual sharding key generator segments should exist."), actual);
            int count = 0;
            for (ShardingKeyGeneratorSegment each : actual) {
                ShardingKeyGeneratorAssert.assertIs(assertContext, each, expected.get(count));
                count++;
            }
        }
    }
}
