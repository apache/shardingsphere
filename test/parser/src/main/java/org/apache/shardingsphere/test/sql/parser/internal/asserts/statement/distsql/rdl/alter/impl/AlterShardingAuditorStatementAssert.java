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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.rdl.alter.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingAuditorStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.rdl.ShardingAuditorAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.segment.impl.distsql.rdl.ExpectedShardingAuditor;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.distsql.rdl.alter.AlterShardingAuditorStatementTestCase;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Alter sharding auditor statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterShardingAuditorStatementAssert {
    
    /**
     * Assert alter sharding auditor statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter sharding auditor statement
     * @param expected expected create sharding auditor statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterShardingAuditorStatement actual, final AlterShardingAuditorStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual sharding auditor statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual sharding auditor statement should exist."), actual);
            assertShardingAuditor(assertContext, actual.getAuditorSegments(), expected.getShardingAuditors());
        }
    }
    
    private static void assertShardingAuditor(final SQLCaseAssertContext assertContext, final Collection<ShardingAuditorSegment> actual, final List<ExpectedShardingAuditor> expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual sharding auditor segments should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual sharding auditor segments should exist."), actual);
            int count = 0;
            for (ShardingAuditorSegment each : actual) {
                ShardingAuditorAssert.assertIs(assertContext, each, expected.get(count));
                count++;
            }
        }
    }
}
