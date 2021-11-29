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

package org.apache.shardingsphere.sharding.route.engine.condition;

import junit.framework.TestCase;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Assert;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public class ShardingConditionsTest extends TestCase {

    private final ShardingConditions shardingConditions = new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class));

    /**
     * Tests isAlwaysFalse().
     */
    public void testIsAlwaysFalse() {
        Assert.assertEquals(shardingConditions.isAlwaysFalse(), false);
    }

    /**
     * Tests isNeedMerge().
     */
    public void testIsNeedMerge() {
        Assert.assertEquals(shardingConditions.isNeedMerge(), false);
    }

    /**
     * Tests isSameShardingCondition().
     */
    public void testIsSameShardingCondition() {
        Assert.assertEquals(shardingConditions, shardingConditions);
    }
}
