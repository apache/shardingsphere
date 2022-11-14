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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAuditorSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedShardingAuditor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Sharding auditor assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingAuditorAssert {
    
    /**
     * Assert sharding auditor is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual sharding auditor
     * @param expected expected sharding auditor test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShardingAuditorSegment actual, final ExpectedShardingAuditor expected) {
        assertNotNull(assertContext.getText("Actual sharding auditor segment should exist."), actual.getAlgorithmSegment());
        assertThat(actual.getAuditorName(), is(expected.getAuditorName()));
        AlgorithmAssert.assertIs(assertContext, actual.getAlgorithmSegment(), expected.getAlgorithmSegment());
    }
}
